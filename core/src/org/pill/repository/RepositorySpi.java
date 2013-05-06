package org.pill.repository;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import org.pill.Dependency;
import org.pill.EntityExistsException;
import org.pill.Module;
import org.pill.Release;

/**
 * Service Provider Interface for repositories.
 * <p/>
 * @author Gili Tzabari
 */
public interface RepositorySpi extends Repository
{
	/**
	 * Adds a release to the repository.
	 * <p/>
	 * @param module the release module
	 * @param version the release version
	 * @param path the path of the file associated with the release
	 * @param dependencies the release's dependencies
	 * @return the release
	 * @throws NullPointerException if module, version, file or dependencies are null
	 * @throws IllegalArgumentException if the repository does not contain the module associated with
	 * the release
	 * @throws EntityExistsException if the release already exists
	 * @throws IOException if an I/O error occurs
	 */
	Release insertRelease(Module module, String version, Path path, Set<Dependency> dependencies)
		throws EntityExistsException, IOException;
}
