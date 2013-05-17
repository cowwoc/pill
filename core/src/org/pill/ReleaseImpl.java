package org.pill;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Set;

/**
 * A software release. Each release consists of a single file.
 * <p/>
 * THREAD-SAFETY: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
public final class ReleaseImpl implements Release
{
	private final URI uri;
	private final Module module;
	private final String version;
	private final String filename;
	private final Set<Dependency> dependencies;

	/**
	 * Creates a new Release.
	 * <p/>
	 * @param uri the release identifier
	 * @param module the module associated with the release
	 * @param version the version associated with the release
	 * @param filename the filename of the release
	 * @param dependencies the dependencies associated with the release
	 * <p/>
	 * @throws NullPointerException if uri, module, version or dependencies are null
	 */
	public ReleaseImpl(URI uri, Module module, String version, String filename,
		Set<Dependency> dependencies)
	{
		Preconditions.checkNotNull(uri, "uri may not be null");
		Preconditions.checkNotNull(module, "module may not be null");
		Preconditions.checkNotNull(version, "version may not be null");
		Preconditions.checkNotNull(filename, "filename may not be null");
		Preconditions.checkNotNull(dependencies, "dependencies may not be null");

		this.uri = uri;
		this.module = module;
		this.version = version;
		this.filename = filename;
		this.dependencies = ImmutableSet.copyOf(dependencies);
	}

	@Override
	public URI getUri()
	{
		return uri;
	}

	@Override
	public Module getModule()
	{
		return module;
	}

	@Override
	public String getVersion()
	{
		return version;
	}

	@Override
	public String getFilename()
	{
		return filename;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Set<Dependency> getDependencies()
	{
		return dependencies;
	}

	@Override
	public void copyTo(Path directory, CopyOption... options) throws IOException
	{
		if (!Files.readAttributes(directory, BasicFileAttributes.class).isDirectory())
			throw new NoSuchFileException("directory must refer to an existing directory: " + directory);

		Path source = java.nio.file.Paths.get(getUri());
		Path target = directory.resolve(source.getFileName().toString());
		FileTime targetModifiedTime;
		try
		{
			targetModifiedTime = Files.getLastModifiedTime(target);
		}
		catch (NoSuchFileException unused)
		{
			targetModifiedTime = FileTime.fromMillis(-1);
		}
		if (!Arrays.asList(options).contains(PerformanceCopyOption.SKIP_NEWER)
			|| Files.getLastModifiedTime(source).compareTo(targetModifiedTime) > 0)
		{
			Files.copy(source, target, options);
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof ReleaseImpl))
			return false;
		ReleaseImpl other = (ReleaseImpl) o;
		return uri.equals(other.getUri());
	}

	@Override
	public int hashCode()
	{
		return uri.hashCode();
	}

	@Override
	public String toString()
	{
		return new ToJsonString(ReleaseImpl.class, this).toString();
	}
}
