/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.ecf.internal.mylyn.ui.repository;

import java.util.UUID;
import org.eclipse.ecf.internal.mylyn.ui.Activator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.repositories.RepositoryLocation;
import org.eclipse.mylyn.internal.commons.repositories.InMemoryCredentialsStore;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @author Steffen Pingel
 */
public class ContainerRepositoryWizard extends Wizard implements INewWizard {

	private ContainerRepository original;
	private RepositoryLocation workingCopy;
	private boolean isNew;

	public ContainerRepositoryWizard(ContainerRepository repository) {
		this.original = repository;
		setNeedsProgressMonitor(true);
		if (isNew()) {
			setWindowTitle("New XMPP Connection");
			setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ));
		} else {
			setWindowTitle("XMPP Connection Properties");
			setDefaultPageImageDescriptor(TasksUiImages.BANNER_REPOSITORY_SETTINGS);
		}
	}

	public ContainerRepositoryWizard() {
		this(new ContainerRepository());
		this.isNew = true;
	}

	@Override
	public void addPages() {
		ContainerRepositoryWizardPage page = new ContainerRepositoryWizardPage("newContainerConnection");
		page.init(getModel());
		initPage(page);
		addPage(page);
	}

	protected void initPage(ContainerRepositoryWizardPage page) {
		// ignore		
	}

	public RepositoryLocation getModel() {
		if (workingCopy == null) {
			workingCopy = new RepositoryLocation(original.getLocation());
			if (workingCopy.getProperty(RepositoryLocation.PROPERTY_ID) == null) {
				workingCopy.setProperty(RepositoryLocation.PROPERTY_ID, UUID.randomUUID().toString());
			}
			workingCopy.setCredentialsStore(new InMemoryCredentialsStore(workingCopy.getCredentialsStore()));
		}
		return workingCopy;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// ignore
	}

	@Override
	public boolean performFinish() {
		getModel().setIdPreservingCredentialsStore(getModel().getUrl() + ";" + getModel().getUserName());
		original.getLocation().apply(getModel());
		if (isNew()) {
			Activator.getDefault().getRepositoryManager().addRepository(original);
		}
		return true;
	}

	private boolean isNew() {
		return isNew;
	}

}
