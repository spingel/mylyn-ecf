package org.eclipse.ecf.internal.mylyn.ui.repository;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.mylyn.commons.repositories.RepositoryLocation;
import org.eclipse.mylyn.commons.ui.team.RepositoryLocationPart;

public class ContainerRepositoryPart extends RepositoryLocationPart {

	public ContainerRepositoryPart(RepositoryLocation workingCopy) {
		super(workingCopy);
	}

	@Override
	protected UpdateValueStrategy getUrlUpdateValueStrategy() {
		return null;
	}

}
