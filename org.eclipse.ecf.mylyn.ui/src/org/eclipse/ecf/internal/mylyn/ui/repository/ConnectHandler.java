package org.eclipse.ecf.internal.mylyn.ui.repository;

import org.eclipse.core.commands.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConnectHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) selection).getFirstElement();
			if (item instanceof ContainerRepository) {
				ContainerRepository repository = (ContainerRepository) item;
				repository.connect();
			}
		}
		return null;
	}

}