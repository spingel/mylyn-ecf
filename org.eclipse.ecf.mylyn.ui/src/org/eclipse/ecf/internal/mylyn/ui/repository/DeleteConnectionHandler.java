package org.eclipse.ecf.internal.mylyn.ui.repository;

import org.eclipse.core.commands.*;
import org.eclipse.ecf.internal.mylyn.ui.Activator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteConnectionHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) selection).getFirstElement();
			if (item instanceof ContainerRepository) {
				ContainerRepository repository = (ContainerRepository) item;
				Activator.getDefault().getRepositoryManager().removeRepository(repository);
			}
		}
		return null;
	}

}