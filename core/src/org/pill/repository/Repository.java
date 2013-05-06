package org.pill.repository;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.annotation.Nullable;
import org.pill.*;

/**
 * A module repository.
 * <p/>
 * @author Gili Tzabari
 */
public interface Repository
{
	/**
	 * Adds a module to the repository.
	 * <p/>
	 * @param name the module name
	 * @return the inserted Module
	 * @throws NullPointerException if name is null
	 * @throws IllegalArgumentException if name is an empty string
	 * @throws EntityExistsException if the module already exists
	 * @throws IOException if an I/O error occurs
	 */
	Module insertModule(String name)
		throws EntityExistsException, IOException;

	/**
	 * Adds a release to the repository.
	 * <p/>
	 * @param module the module name of the release
	 * @param version the version number of the release
	 * @param path the path of the file associated with the release
	 * @return the Release builder
	 * @throws NullPointerException if module or version are null
	 * @throws IllegalArgumentException if version is an empty string
	 */
	ReleaseBuilder insertRelease(Module module, String version, Path path);

	/**
	 * Looks up a module by its name.
	 * <p/>
	 * @param name the module name
	 * @return null if the module was not found
	 * @throws IOException if an I/O error occurs
	 * @throws NullPointerException if name is null
	 * @throws IllegalArgumentException if name is an empty string
	 */
	@Nullable
	Module getModule(String name) throws IOException;

	/**
	 * Lists all releases associated with a module.
	 * <p/>
	 * @param module the module
	 * @return a list of releases
	 * @throws NullPointerException if module is null
	 * @throws IOException if an I/O error occurs
	 * @throws IllegalArgumentException if the module was not found
	 */
	List<URI> getReleases(Module module) throws IOException;

	/**
	 * Looks up a release in the repository.
	 * <p/>
	 * @param module the module name
	 * @param version the module version
	 * @return null if the release was not found
	 * @throws NullPointerException if module or version are null
	 * @throws IllegalArgumentException if version is an empty string
	 * @throws IOException if an I/O error occurs
	 */
	@Nullable
	URI getReleaseUri(Module module, String version) throws IOException;

	/**
	 * Looks up a release in the repository.
	 * <p/>
	 * @param uri the release URI
	 * @return null if the release was not found
	 * @throws NullPointerException if module or version are null
	 * @throws IllegalArgumentException if uri is not a valid release URI
	 */
	@Nullable
	Release getRelease(URI uri);

	/**
	 * Indicates if a file exists.
	 * <p/>
	 * @param uri the file URI
	 * @return true if the file exists
	 * @throws IOException if an I/O error occurs
	 */
	boolean fileExists(URI uri) throws IOException;

	/**
	 * Returns a SeekableByteChannel for reading a file.
	 * <p/>
	 * @param uri the file URI
	 * @return a read-only SeekableByteChannel
	 * @throws IllegalArgumentException if uri does not refer to a file
	 * @throws NoSuchFileException if the file does not exist <i>(optional specific exception)</i>
	 * @throws IOException if an I/O error occurs opening the file
	 */
	SeekableByteChannel newByteChannel(URI uri) throws NoSuchFileException, IOException;

	/**
	 * Returns a file's attributes.
	 * <p/>
	 * @param uri the file's URI
	 * @return the file's attributes
	 * @throws NoSuchFileException if the file does not exist <i>(optional specific exception)</i>
	 * @throws IOException if an I/O error occurs
	 */
	BasicFileAttributes readAttributes(URI uri) throws NoSuchFileException, IOException;

	/**
	 * Removes a module from the repository.
	 * <p/>
	 * @param module the module to removeRelease
	 * @throws NullPointerException if module is null
	 * @throws IllegalStateException if the module contains releases that must be removed first
	 * @throws EntityNotFoundException if the repository does not contain the module
	 * @throws IOException if an I/O error occurs
	 */
	void removeModule(Module module)
		throws EntityNotFoundException, IOException;

	/**
	 * Removes a release from the repository.
	 * <p/>
	 * @param release the release to removeRelease
	 * @throws NullPointerException if release is null
	 * @throws EntityNotFoundException if the repository does not contain the release
	 * @throws IOException if an I/O error occurs
	 */
	void removeRelease(Release release)
		throws EntityNotFoundException, IOException;
}
