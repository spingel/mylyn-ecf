package org.eclipse.ecf.internal.mylyn.ui.repository;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.repositories.RepositoryLocation;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;

public class RepositoryManager {

	private List<RepositoryListener> listeners = new CopyOnWriteArrayList<RepositoryListener>();

	private List<ContainerRepository> repositories = new CopyOnWriteArrayList<ContainerRepository>();

	private final File cacheFile;

	public RepositoryManager(File cacheFile) {
		this.cacheFile = cacheFile;
		read();
	}

	public void addListener(RepositoryListener listener) {
		listeners.add(listener);
	}

	public void addRepository(ContainerRepository repository) {
		repositories.add(repository);
		for (RepositoryListener listener : listeners) {
			listener.repositoryAdded(repository);
		}
	}

	public void removeListener(RepositoryListener listener) {
		listeners.remove(listener);
	}

	public void removeRepository(ContainerRepository repository) {
		repositories.remove(repository);
		for (RepositoryListener listener : listeners) {
			listener.repositoryRemoved(repository);
		}
	}

	public void read() {
		if (cacheFile == null || !cacheFile.exists()) {
			return;
		}

		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(cacheFile));
			int size = in.readInt();
			for (int i = 0; i < size; i++) {
				String url = (String) in.readObject();
				Map<String, String> data = (Map<String, String>) in.readObject();
				if (url != null && data != null) {
					addRepository(new ContainerRepository(new RepositoryLocation(data)));
				}
			}
		} catch (Throwable e) {
			StatusHandler.log(new Status(IStatus.WARNING, ITasksCoreConstants.ID_PLUGIN, "The respository configuration cache could not be read", e)); //$NON-NLS-1$
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

	public void write() {
		if (cacheFile == null) {
			return;
		}

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(cacheFile));
			out.writeInt(repositories.size());
			for (ContainerRepository repository : repositories) {
				out.writeObject(repository.getLocation().getUrl());
				out.writeObject(repository.getLocation().getProperties());
			}
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.WARNING, ITasksCoreConstants.ID_PLUGIN, "The respository configuration cache could not be written", e)); //$NON-NLS-1$
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public ContainerRepository[] getRepositories() {
		return repositories.toArray(new ContainerRepository[0]);
	}

}
