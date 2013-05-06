package org.pill.repository.local;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import org.pill.repository.RepositorySpi;

/**
 * A Release file system.
 * <p/>
 * @author Gili Tzabari
 */
public final class ReleaseFileSystem extends FileSystem
{
	private final ReleaseFileSystemProvider provider;
	private final URI release;
	private final RepositorySpi repository;
	private final Method toRegexPattern;
//	private ReleasePath currentPath;
	private boolean open;

	/**
	 * Creates a new ReleaseFileSystem.
	 * <p/>
	 * @param provider the ReleaseFileSystemProvider
	 * @param release the release URI
	 * @param repository the repository associated with the filesystem
	 * @throws NullPointerException if provider or repository are null
	 * @throws IllegalArgumentException if {@code release} is not a valid release URI
	 */
	public ReleaseFileSystem(ReleaseFileSystemProvider provider, URI release, RepositorySpi repository)
	{
		Preconditions.checkNotNull(provider, "provider may not be null");
		Preconditions.checkNotNull(repository, "repository may not be null");
		Preconditions.checkArgument(provider.isRelease(release), "Invalid release: "
			+ release);

		this.provider = provider;
		this.release = release;
		this.repository = repository;
//		this.currentPath = getPath("/");

		try
		{
			this.toRegexPattern = Class.forName("sun.nio.fs.Globs").getDeclaredMethod("toRegexPattern",
				String.class, boolean.class);
		}
		catch (ReflectiveOperationException e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * @return the URI of the Release
	 */
	public URI getRelease()
	{
		return release;
	}

//	/**
//	 * Sets the path relative to which paths should be resolved.
//	 * <p/>
//	 * @param currentPath the path relative to which paths should be resolved
//	 * @see Path#toAbsolutePath()
//	 * @throws NullPointerException if currentPath is null
//	 */
//	public void setCurrentPath(ReleasePath currentPath)
//	{
//		Preconditions.checkNotNull(currentPath, "currentPath may not be null");
//
//		this.currentPath = currentPath;
//	}
//
//	/**
//	 * Returns the path relative to which paths should be resolved. The default value is {@code "/"}.
//	 * <p/>
//	 * @return the path relative to which paths should be resolved
//	 * @see Path#toAbsolutePath()
//	 */
//	public ReleasePath getCurrentPath()
//	{
//		return currentPath;
//	}
	/**
	 * @return the Repository associated with the filesystem
	 */
	public RepositorySpi getRepository()
	{
		return repository;
	}

	@Override
	public FileSystemProvider provider()
	{
		return provider;
	}

	@Override
	public void close() throws IOException
	{
		if (open)
		{
			open = false;
			provider.close(this);
		}
	}

	@Override
	public boolean isOpen()
	{
		return open;
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	@Override
	public String getSeparator()
	{
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories()
	{
		return Collections.<Path>singleton(getPath("/"));
	}

	@Override
	public Iterable<FileStore> getFileStores()
	{
		return Collections.emptyList();
	}

	@Override
	public Set<String> supportedFileAttributeViews()
	{
		return Collections.singleton("basic");
	}

	@Override
	public ReleasePath getPath(String first, String... more)
	{
		Preconditions.checkNotNull(first, "first may not be null");
		Preconditions.checkNotNull(more, "more may not be null");

		String path = Joiner.on("/").join(Lists.asList(first, more));
		return new ReleasePath(this, path);
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern)
	{
		Preconditions.checkNotNull(syntaxAndPattern, "syntaxAndPattern may not be null");

		String[] tokens = syntaxAndPattern.split(":");
		if (tokens.length != 2)
		{
			throw new IllegalArgumentException("syntaxAndPattern must take the form \"syntax:pattern\": "
				+ syntaxAndPattern);
		}
		final Pattern pattern;
		switch (tokens[0])
		{
			case "glob":
			{
				try
				{
					pattern = Pattern.compile((String) toRegexPattern.invoke(tokens[1], false));
				}
				catch (ReflectiveOperationException e)
				{
					throw new AssertionError(e);
				}
				break;
			}
			case "regex":
			{
				pattern = Pattern.compile(tokens[1]);
				break;
			}
			default:
				throw new UnsupportedOperationException("syntax: " + tokens[0]);
		}
		return new PathMatcher()
		{
			@Override
			public boolean matches(Path path)
			{
				Preconditions.checkNotNull(path, "path may not be null");

				return pattern.matcher(path.toString()).matches();
			}
		};
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchService newWatchService() throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
