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

import org.eclipse.core.commands.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.dialogs.ValidatableWizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Steffen Pingel
 */
public class RepositoryPropertiesHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) selection).getFirstElement();
			if (item instanceof ContainerRepository) {
				ContainerRepository repository = (ContainerRepository) item;
				return openPropertiesDialog(repository);
			}
		}
		return null;
	}

	public static int openPropertiesDialog(ContainerRepository repository) {
		Wizard wizard = new ContainerRepositoryWizard(repository);
		ValidatableWizardDialog dialog = new ValidatableWizardDialog(WorkbenchUtil.getShell(), wizard);
		dialog.create();
		return dialog.open();
	}

}
