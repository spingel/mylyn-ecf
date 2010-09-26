package org.eclipse.ecf.internal.mylyn.ui.navigator;

import org.eclipse.ecf.internal.mylyn.ui.Activator;
import org.eclipse.ecf.internal.mylyn.ui.repository.*;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.repositories.RepositoryCategory;
import org.eclipse.swt.widgets.Display;

public class RepositoryContentProvider implements ITreeContentProvider {

	private class Listener extends RepositoryListener {

		protected void refresh() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
						viewer.refresh();
					}
				}
			});
		}

		@Override
		public void repositoryAdded(ContainerRepository repository) {
			refresh();
		}

		@Override
		public void repositoryRemoved(ContainerRepository repository) {
			refresh();
		}

	}

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private RepositoryManager repositoryManager;

	private RepositoryListener listener;

	//	private class ContainerListener implements IContainerListener {
	//		public void handleEvent(IContainerEvent event) {
	//			refresh();
	//		}
	//
	//		protected void refresh() {
	//			Display.getDefault().asyncExec(new Runnable() {
	//				public void run() {
	//					if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
	//						viewer.refresh();
	//					}
	//				}
	//			});
	//		}
	//	}

	private Viewer viewer;

	public RepositoryContentProvider() {
		repositoryManager = Activator.getDefault().getRepositoryManager();
	}

	public void dispose() {
		if (listener != null) {
			repositoryManager.removeListener(listener);
			listener = null;
		}
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof RepositoryCategory) {
			RepositoryCategory category = (RepositoryCategory) parentElement;
			if (RepositoryCategory.ID_CATEGORY_ALL.equals(category.getId())) {
				return repositoryManager.getRepositories();
			} else if (RepositoryCategory.ID_CATEGORY_OTHER.equals(category.getId())) {
				return repositoryManager.getRepositories();
			}
		}
		return EMPTY_ARRAY;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (listener != null) {
			repositoryManager.removeListener(listener);
			listener = null;
		}
		this.viewer = viewer;
		if (newInput != null) {
			listener = new Listener();
			repositoryManager.addListener(listener);
		}
	}
}
