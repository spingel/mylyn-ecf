package org.eclipse.ecf.internal.mylyn.ui.repository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.events.*;
import org.eclipse.ecf.internal.provider.xmpp.ui.Messages;
import org.eclipse.ecf.presence.*;
import org.eclipse.ecf.presence.im.*;
import org.eclipse.ecf.presence.ui.MessagesView;
import org.eclipse.ecf.presence.ui.MultiRosterView;
import org.eclipse.ecf.ui.actions.AsynchContainerConnectAction;
import org.eclipse.ecf.ui.dialogs.IDCreateErrorDialog;
import org.eclipse.ecf.ui.util.PasswordCacheHelper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.repositories.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.auth.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.auth.UsernamePasswordCredentials;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class ContainerRepository {

	private IConnectContext connectContext;

	private IContainer container;

	private IChatMessageSender icms;

	private ITypingMessageSender itms;

	private RepositoryLocation location;

	private final PropertyChangeListener locationChangeListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent event) {
		}
	};

	protected IIncomingFileTransferRequestListener requestListener = new IIncomingFileTransferRequestListener() {
		public void handleFileTransferRequest(final IFileTransferRequestEvent event) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					final String username = event.getRequesterID().getName();
					final IFileTransferInfo transferInfo = event.getFileTransferInfo();
					final String fileName = transferInfo.getFile().getName();
					final Object[] bindings = new Object[] {username, fileName, ((transferInfo.getFileSize() == -1) ? "unknown" //$NON-NLS-1$
							: (transferInfo.getFileSize() + " bytes")), //$NON-NLS-1$
							(transferInfo.getDescription() == null) ? "none" //$NON-NLS-1$
									: transferInfo.getDescription()};
					Shell shell = WorkbenchUtil.getShell();
					if (MessageDialog.openQuestion(shell, NLS.bind(Messages.XMPPConnectWizard_FILE_RECEIVE_TITLE, username), NLS.bind(Messages.XMPPConnectWizard_FILE_RECEIVE_MESSAGE, bindings))) {
						final FileDialog fd = new FileDialog(shell, SWT.OPEN);
						// XXX this should be some default path gotten from
						// preference. For now we'll have it be the user.home
						// system property
						fd.setFilterPath(System.getProperty("user.home")); //$NON-NLS-1$
						fd.setFileName(fileName);
						final int suffixLoc = fileName.lastIndexOf('.');
						if (suffixLoc != -1) {
							final String ext = fileName.substring(fileName.lastIndexOf('.'));
							fd.setFilterExtensions(new String[] {ext});
						}
						fd.setText(NLS.bind(Messages.XMPPConnectWizard_FILE_SAVE_TITLE, username));
						final String res = fd.open();
						if (res == null)
							event.reject();
						else {
							try {
								final FileOutputStream fos = new FileOutputStream(new File(res));
								event.accept(fos, new IFileTransferListener() {
									public void handleTransferEvent(IFileTransferEvent event) {
										// XXX Should have some some UI
										// for transfer events
										if (event instanceof IIncomingFileTransferReceiveDoneEvent) {
											try {
												fos.close();
											} catch (final IOException e) {
											}
										}
									}
								});
							} catch (final Exception e) {
								MessageDialog.openError(shell, Messages.XMPPConnectWizard_RECEIVE_ERROR_TITLE, NLS.bind(Messages.XMPPConnectWizard_RECEIVE_ERROR_MESSAGE, new Object[] {fileName, username, e.getLocalizedMessage()}));
							}
						}
					} else
						event.reject();
				}
			});
		}

	};

	private ID targetID;

	private boolean listenersRegistered;

	public ContainerRepository() {
		this(new RepositoryLocation());
	}

	public ContainerRepository(RepositoryLocation location) {
		setLocation(location);
	}

	protected void cachePassword(final String connectID, String password) {
		if (password != null && !password.equals("")) {
			final PasswordCacheHelper pwStorage = new PasswordCacheHelper(connectID);
			pwStorage.savePassword(password);
		}
	}

	public boolean connect() {
		try {
			this.container = ContainerFactory.getDefault().createContainer("ecf.xmpps.smack");
		} catch (ContainerCreateException e) {
			// None
		}

		UsernamePasswordCredentials credentials = getLocation().getCredentials(AuthenticationType.REPOSITORY, UsernamePasswordCredentials.class);
		final String connectID = credentials.getUserName() + ";" + getLocation().getUrl();
		final String password = credentials.getPassword();

		connectContext = ConnectContextFactory.createPasswordConnectContext(password);

		try {
			targetID = IDFactory.getDefault().createID(container.getConnectNamespace(), connectID);
		} catch (final IDCreateException e) {
			new IDCreateErrorDialog(null, connectID, e).open();
			return false;
		}

		if (!listenersRegistered) {
			listenersRegistered = true;
			registerListeners();
		}

		// Connect
		new AsynchContainerConnectAction(container, targetID, connectContext, null, new Runnable() {
			public void run() {
				cachePassword(connectID, password);
			}
		}).run();

		return true;
	}

	private void displayMessage(IChatMessageEvent e) {
		final IChatMessage message = e.getChatMessage();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				MessagesView view = (MessagesView) workbench.getActiveWorkbenchWindow().getActivePage().findView(MessagesView.VIEW_ID);
				if (view != null) {
					final IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) view.getSite().getAdapter(IWorkbenchSiteProgressService.class);
					view.openTab(icms, itms, targetID, message.getFromID());
					view.showMessage(message);
					service.warnOfContentChange();
				} else {
					try {

						final IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
						view = (MessagesView) page.showView(MessagesView.VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
						if (!page.isPartVisible(view)) {
							final IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) view.getSite().getAdapter(IWorkbenchSiteProgressService.class);
							service.warnOfContentChange();
						}
						view.openTab(icms, itms, targetID, message.getFromID());
						view.showMessage(message);
					} catch (final PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void displayTypingNotification(final ITypingMessageEvent e) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final MessagesView view = (MessagesView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(MessagesView.VIEW_ID);
				if (view != null)
					view.displayTypingNotification(e);
			}
		});
	}

	public IContainer getContainer() {
		return container;
	}

	public RepositoryLocation getLocation() {
		return location;
	}

	private void openView() {
		try {
			final MultiRosterView view = (MultiRosterView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MultiRosterView.VIEW_ID);
			view.addContainer(container);
		} catch (final PartInitException e) {
			e.printStackTrace();
		}
	}

	protected void registerListeners() {
		final IPresenceContainerAdapter adapter = (IPresenceContainerAdapter) container.getAdapter(IPresenceContainerAdapter.class);

		container.addListener(new IContainerListener() {
			public void handleEvent(IContainerEvent event) {
				if (event instanceof IContainerConnectedEvent) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							openView();
						}
					});
				}
			}
		});

		final IChatManager icm = adapter.getChatManager();
		icms = icm.getChatMessageSender();
		itms = icm.getTypingMessageSender();

		icm.addMessageListener(new IIMMessageListener() {
			public void handleMessageEvent(IIMMessageEvent e) {
				if (e instanceof IChatMessageEvent) {
					displayMessage((IChatMessageEvent) e);
				} else if (e instanceof ITypingMessageEvent) {
					displayTypingNotification((ITypingMessageEvent) e);
				}
			}
		});

		final ISendFileTransferContainerAdapter ioftca = (ISendFileTransferContainerAdapter) container.getAdapter(ISendFileTransferContainerAdapter.class);
		ioftca.addListener(requestListener);
	}

	public void setLocation(RepositoryLocation newLocation) {
		RepositoryLocation oldLocation = location;
		if (oldLocation != null) {
			oldLocation.removeChangeListener(locationChangeListener);
		}
		location = newLocation;
		if (location != null) {
			location.addChangeListener(locationChangeListener);
		}
	}

}
