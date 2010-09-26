package org.eclipse.ecf.internal.mylyn.ui.navigator;

import org.eclipse.ecf.internal.mylyn.ui.repository.ContainerRepository;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.graphics.Image;

public class RepositoryLabelProvider extends LabelProvider {

	public Image getImage(Object element) {
		if (element instanceof ContainerRepository) {
			return CommonImages.getImage(CommonImages.PERSON);
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof ContainerRepository) {
			return ((ContainerRepository) element).getLocation().getLabel();
		}
		return null;
	}

}
