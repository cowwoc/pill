package org.pill.repository;

/**
 * A collection of one or more repositories.
 * <p/>
 * @author Gili Tzabari
 */
public final class CompoundRepository
{
//	private final Set<Repository> children = new HashSet<>();
//
//	/**
//	 * Adds a child repository.
//	 * <p/>
//	 * @param repository the repository to add
//	 * @return true if this object did not contain the repository
//	 */
//	public boolean addRepository(Repository repository)
//	{
//		return children.add(repository);
//	}
//
//	/**
//	 * Removes a child repository.
//	 * <p/>
//	 * @param repository the repository to remove
//	 * @return true if the this object contained the repository
//	 */
//	public boolean removeRepository(Repository repository)
//	{
//		return children.remove(repository);
//	}
//
//	/**
//	 * Looks up a module in all repositories.
//	 * <p/>
//	 * @param module the module name
//	 * @return null if the module was not found in any of the repositories; otherwise, a list of
//	 * releases from the first repository to return a non-null value
//	 * @throws NullPointerException if module is null
//	 * @throws IOException if an error occurs reading the module files
//	 */
//	public List<Release> getReleases(Module module)
//		throws IOException
//	{
//		for (Repository child: children)
//		{
//			List<Release> result = child.getReleases(module);
//			if (result != null)
//				return result;
//		}
//		return null;
//	}
//
//	/**
//	 * Looks up a release in all repositories.
//	 * <p/>
//	 * @param module the module name
//	 * @param version the module version
//	 * @return null if the release was not found in any of the repositories
//	 * @throws NullPointerException if module or version are null
//	 * @throws IOException if an error occurs reading the module files
//	 */
//	public Release getRelease(Module module, String version)
//		throws IOException
//	{
//		for (Repository child: children)
//		{
//			Release result = child.getRelease(module, version);
//			if (result != null)
//				return result;
//		}
//		return null;
//	}
//
//	/**
//	 * Adds a module to all repositories.
//	 * <p/>
//	 * @param name the module name
//	 * @return a map from a repository to the Module that was inserted
//	 * @throws NullPointerException if name is null
//	 * @throws EntityExistsException if the module already existed in any of the repositories
//	 * @throws IOException if an I/O error occurs
//	 */
//	public Map<Repository, Module> insertModule(String name)
//		throws EntityExistsException, IOException
//	{
//		Map<Repository, Module> result = new HashMap<>();
//		for (Repository child: children)
//		{
//			Module module = child.insertModule(name);
//			result.put(child, module);
//		}
//		return result;
//	}
//
//	/**
//	 * Adds a release to all repositories.
//	 * <p/>
//	 * @param module the module name of the release
//	 * @param version the version number of the release
//	 * @throws NullPointerException if module or version are null
//	 * @throws IllegalArgumentException if any of the repositories does not contain the module
//	 * associated with release
//	 */
//	public CompoundReleaseBuilder insertRelease(Module module, String version)
//	{
//		for (Repository child: children)
//			child.insert(release);
//	}
//
//	/**
//	 * Removes a module from all repositories.
//	 * <p/>
//	 * @param module the module to remove
//	 * @throws EntityNotFoundException if any of the repositories does not contain the module
//	 * @throws IllegalStateException if the module contains releases that must be removed first
//	 * removed
//	 * @throws IOException if an I/O error occurs
//	 */
//	@Override
//	public void remove(Module module)
//		throws EntityNotFoundException, IOException
//	{
//		for (Repository child: children)
//			child.remove(module);
//	}
//
//	/**
//	 * Removes a release from all repositories.
//	 * <p/>
//	 * @param release the release to remove
//	 * @throws EntityNotFoundException if any of the repositories does not contain the release
//	 * @throws IOException if an I/O error occurs
//	 */
//	@Override
//	public void remove(Release release)
//		throws EntityNotFoundException, IOException
//	{
//		for (Repository child: children)
//			child.remove(release);
//	}
//
//	@Override
//	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
//	public boolean equals(Object o)
//	{
//		if (!(o instanceof CompoundRepository))
//			return false;
//		final CompoundRepository other = (CompoundRepository) o;
//		return this.children.equals(other.children);
//	}
//
//	@Override
//	public int hashCode()
//	{
//		return this.children.hashCode();
//	}
}
