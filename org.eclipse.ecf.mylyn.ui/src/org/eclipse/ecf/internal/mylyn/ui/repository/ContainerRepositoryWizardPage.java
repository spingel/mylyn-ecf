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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.mylyn.commons.repositories.RepositoryLocation;
import org.eclipse.mylyn.commons.ui.team.RepositoryLocationPart;
import org.eclipse.mylyn.commons.ui.team.RepositoryWizardPage;

/**
 * @author Steffen Pingel
 */
public class ContainerRepositoryWizardPage extends RepositoryWizardPage {

	private RepositoryLocation model;

	public ContainerRepositoryWizardPage(String pageName) {
		super(pageName);
		setTitle("XMPP Connection Properties");
		setElement(new IAdaptable() {
			public Object getAdapter(Class adapter) {
				if (adapter == RepositoryLocation.class) {
					return getModel();
				}
				return null;
			}
		});
	}

	@Override
	protected RepositoryLocationPart doCreateRepositoryPart() {
		ContainerRepositoryPart part = new ContainerRepositoryPart(getModel());
		return part;
	}

	public RepositoryLocation getModel() {
		return model;
	}

	@Override
	public ContainerRepositoryPart getPart() {
		return (ContainerRepositoryPart) super.getPart();
	}

	public void init(RepositoryLocation model) {
		this.model = model;
	}

}
