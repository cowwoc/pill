package org.pill;

import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Set;

/**
 * A software release. Each release consists of a single file.
 * <p/>
 * THREAD-SAFETY: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
public interface Release
{
	/**
	 * @return the release identifier
	 */
	URI getUri();

	/**
	 * @return the module associated with the release
	 */
	Module getModule();

	/**
	 * @return the version associated with the release
	 */
	String getVersion();

	/**
	 * @return the filename of the release
	 */
	String getFilename();

	/**
	 * @return the release dependencies
	 */
	Set<Dependency> getDependencies();

	/**
	 * Copies the module to a directory.
	 * <p/>
	 * @param directory the directory to copy the module into
	 * @param options options specifying how the copy should be done
	 * @throws NoSuchFileException if directory is not an existing directory
	 * @throws IOException if an I/O error occurs
	 * @see PerformanceCopyOption
	 */
	void copyTo(Path directory, CopyOption... options) throws IOException;
}
