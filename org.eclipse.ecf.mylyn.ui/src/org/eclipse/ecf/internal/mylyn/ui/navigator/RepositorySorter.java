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

package org.eclipse.ecf.internal.mylyn.ui.navigator;

import org.eclipse.ecf.internal.mylyn.ui.repository.ContainerRepository;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Steffen Pingel
 */
public class RepositorySorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ContainerRepository && e2 instanceof ContainerRepository) {
			ContainerRepository t1 = (ContainerRepository) e1;
			ContainerRepository t2 = (ContainerRepository) e2;

			String label1 = t1.getLocation().getLabel();
			String label2 = t2.getLocation().getLabel();
			return getComparator().compare(label1, label2);
		}
		return super.compare(viewer, e1, e2);
	}

}