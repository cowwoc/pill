package org.pill;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.pill.repository.RepositorySpi;

/**
 * Builds a Release.
 * <p/>
 * @author Gili Tzabari
 */
@SuppressWarnings(value = "PublicInnerClass")
public class ReleaseBuilder
{
	private final RepositorySpi repository;
	private final Module module;
	private final String version;
	private final Path path;
	private final Set<Dependency> dependencies = new HashSet<>();

	/**
	 * Creates a new RemoteRelease builder.
	 * <p/>
	 * @param repository the repository to insert into
	 * @param module the release module
	 * @param version the release version
	 * @throws NullPointerException if module or version are null
	 * @throws IllegalArgumentException if version is an empty string
	 */
	public ReleaseBuilder(RepositorySpi repository, Module module, String version, Path path)
	{
		this.repository = Preconditions.checkNotNull(repository, "repository may not be null");
		this.module = Preconditions.checkNotNull(module, "module may not be null");
		this.version = Preconditions.checkNotNull(version, "version may not be null");
		Preconditions.checkArgument(!version.isEmpty(), "version may not be an empty string");
		this.path = Preconditions.checkNotNull(path, "path may not be null");
	}

	/**
	 * Adds a module dependency.
	 * <p/>
	 * @param dependency the dependency to add to the release
	 * @return the builder
	 * @throws NullPointerException if dependency is null
	 */
	public ReleaseBuilder addDependency(Dependency dependency)
	{
		Preconditions.checkNotNull(dependency, "dependency may not be null");

		dependencies.add(dependency);
		return this;
	}

	/**
	 * Inserts the release.
	 * <p/>
	 * @return the RemoteRelease
	 * <p/>
	 * @throws IllegalArgumentException if files are empty or the repository does not contain the
	 * module associated with release
	 * @throws EntityExistsException if the release already exists
	 * @throws IOException if an I/O error occurs
	 */
	public Release build() throws EntityExistsException, IOException
	{
		return repository.insertRelease(module, version, path, dependencies);
	}
}
