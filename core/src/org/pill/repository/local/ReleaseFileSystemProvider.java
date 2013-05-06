package org.pill.repository.local;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.pill.repository.Repository;
import org.pill.repository.RepositorySpi;

/**
 * Release file system provider.
 * <p/>
 * @author Gili Tzabari
 */
@Singleton
public class ReleaseFileSystemProvider extends FileSystemProvider
{
	private static final String packageName = LocalRepository.class.getPackage().getName();
	private final Map<URI, ReleaseFileSystem> cache = new ConcurrentHashMap<>();
	private final RepositorySpi localRepository;

	/**
	 * Creates a new ReleaseFileSystemProvider without Guice.
	 */
	public ReleaseFileSystemProvider()
	{
		this.localRepository = LocalRepository.getInstance();
	}

	@Override
	public String getScheme()
	{
		return getClass().getPackage().getName() + ".release";
	}

	@Override
	public ReleaseFileSystem newFileSystem(URI uri, Map<String, ?> env)
	{
		Preconditions.checkNotNull(uri, "uri may not be null");
		Preconditions.checkNotNull(env, "env may not be null");

		RepositorySpi repository = (RepositorySpi) env.get("repository");
		ReleaseFileSystem result = new ReleaseFileSystem(this, uri, repository);
		cache.put(uri, result);
		return result;
	}

	@Override
	public FileSystem getFileSystem(URI uri)
	{
		Preconditions.checkNotNull(uri, "uri may not be null");

		FileSystem result = cache.get(uri);
		if (result == null)
			throw new FileSystemNotFoundException(uri.toString());
		return result;
	}

	/**
	 * Closes the file-system.
	 * <p/>
	 * @param filesystem the file-system
	 */
	void close(ReleaseFileSystem filesystem)
	{
		Preconditions.checkNotNull(filesystem, "filesystem may not be null");

		cache.remove(filesystem.getRelease());
	}

	/**
	 * Indicates if a URI represents a release.
	 * <p/>
	 * @param uri the URI
	 * @return true if the URI represents a release
	 * @throws IllegalArgumentException if the URI does not reference this repository
	 */
	public boolean isRelease(URI uri)
	{
		// Releases are identified by "<package>.release:<id>" where <package> is the current package
		// and <id> is the database identifier.
		if (!uri.getScheme().startsWith(packageName + ".release"))
			return false;
		String schemeSpecific = uri.getSchemeSpecificPart();
		int index = schemeSpecific.indexOf(":");
		if (index != -1)
			return false;
		try
		{
			Long.parseLong(schemeSpecific);
			return true;
		}
		catch (NumberFormatException unused)
		{
			return false;
		}
	}

	@Override
	public Path getPath(URI uri)
	{
		if (!isRelease(uri))
			throw new IllegalArgumentException("URI scheme does not identify this provider: " + uri);
		ReleaseFileSystem fs = cache.get(uri);
		if (fs == null)
		{
			Map<String, ?> env = ImmutableMap.<String, Object>builder().
				put("repository", localRepository).build();
			fs = newFileSystem(uri, env);
		}
		return fs.getPath(localRepository.getRelease(uri).getFilename());
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
		FileAttribute<?>... attrs) throws IOException
	{
		// URI syntax: org.pill.repository.local.release:<id>:<path>
		Preconditions.checkNotNull(path, "path may not be null");
		Preconditions.checkNotNull(options, "options may not be null");
		Preconditions.checkNotNull(attrs, "attrs may not be null");
		if (!(path instanceof ReleasePath))
		{
			throw new ProviderMismatchException("path's provider was "
				+ path.getFileSystem().provider().getClass().getName() + " instead of "
				+ getClass().getName());
		}

		// Validate options according to http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#newByteChannel%28java.nio.file.Path,%20java.util.Set,%20java.nio.file.attribute.FileAttribute...%29
		Set<StandardOpenOption> standardOptions = EnumSet.noneOf(StandardOpenOption.class);
		for (OpenOption option : options)
		{
			if (!(option instanceof StandardOpenOption))
				throw new UnsupportedOperationException("Unsupported option: " + option);
			StandardOpenOption standardOption = (StandardOpenOption) option;
			switch (standardOption)
			{
				case READ:
					break;
				case WRITE:
				case DELETE_ON_CLOSE:
					throw new UnsupportedOperationException("Path is read-only");
				case CREATE_NEW:
					throw new FileAlreadyExistsException(path.toString());
				case CREATE:
				case SPARSE:
				case SYNC:
				case DSYNC:
					// ignore since WRITE was not specified
					break;
				case APPEND:
					throw new IllegalArgumentException("READ + APPEND not allowed");
				case TRUNCATE_EXISTING:
				{
					if (options.contains(StandardOpenOption.APPEND))
						throw new IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed");
					break;
				}
				default:
					throw new UnsupportedOperationException("Unexpected option: " + standardOption);
			}
			standardOptions.add(standardOption);
		}
		// Ignore "attrs" because we don't support file attributes

		ReleaseFileSystem filesystem = (ReleaseFileSystem) path.getFileSystem();
		Repository repository = filesystem.getRepository();
		return repository.newByteChannel(URI.create(filesystem.getRelease() + ":" + path.toString()));
	}

	@Override
	public SecureDirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter)
		throws IOException
	{
		Preconditions.checkNotNull(dir, "dir may not be null");
		Preconditions.checkNotNull(filter, "filter may not be null");

		if (!(dir instanceof ReleasePath))
		{
			throw new ProviderMismatchException("dir's provider was "
				+ dir.getFileSystem().provider().getClass().getName() + " instead of "
				+ getClass().getName());
		}
		return new ReleaseDirectoryStream(this, (ReleasePath) dir, filter);
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
		throws IOException
	{
		Preconditions.checkNotNull(dir, "dir may not be null");
		Preconditions.checkNotNull(attrs, "attrs may not be null");

		if (attrs.length > 0)
			throw new UnsupportedOperationException("attrs must be empty");
		Path absolute = dir.toAbsolutePath();
		if (!absolute.equals(absolute.getRoot()))
			throw new IOException("Cannot create " + absolute);
		throw new FileAlreadyExistsException(absolute.toString());
	}

	@Override
	public void delete(Path path) throws IOException
	{
		throw new ReadOnlyFileSystemException();
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException
	{
		Preconditions.checkNotNull(source, "source may not be null");
		Preconditions.checkNotNull(target, "target may not be null");
		Preconditions.checkNotNull(options, "options may not be null");

		if (!(source instanceof ReleasePath))
		{
			throw new ProviderMismatchException("source's provider was "
				+ source.getFileSystem().provider().getClass().getName() + " instead of "
				+ getClass().getName());
		}
		if (!(target instanceof ReleasePath))
		{
			throw new ProviderMismatchException("target's provider was "
				+ target.getFileSystem().provider().getClass().getName() + " instead of "
				+ getClass().getName());
		}
		if (Files.isSameFile(source, target))
			return;
		throw new ReadOnlyFileSystemException();
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException
	{
		Preconditions.checkNotNull(source, "source may not be null");
		Preconditions.checkNotNull(target, "target may not be null");
		Preconditions.checkNotNull(options, "options may not be null");

		if (!(source instanceof ReleasePath))
		{
			throw new ProviderMismatchException("source's provider was "
				+ source.getFileSystem().provider().getClass().getName() + " instead of "
				+ getClass().getName());
		}
		if (!(target instanceof ReleasePath))
		{
			throw new ProviderMismatchException("target's provider was "
				+ target.getFileSystem().provider().getClass().getName() + " instead of "
				+ getClass().getName());
		}
		if (Files.isSameFile(source, target))
			return;
		throw new ReadOnlyFileSystemException();
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException
	{
		return path.equals(path2);
	}

	@Override
	public boolean isHidden(Path path) throws IOException
	{
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException
	{
		throw new UnsupportedOperationException("The path doesn't have a filestore");
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException
	{
		ReleaseFileSystem filesystem = (ReleaseFileSystem) path.getFileSystem();
		Repository repository = filesystem.getRepository();
		if (!repository.fileExists(path.toUri()))
			throw new NoSuchFileException(path.toString());
		for (AccessMode mode : modes)
		{
			switch (mode)
			{
				case READ:
				{
					// Allowed
					break;
				}
				case WRITE:
				case EXECUTE:
					throw new AccessDeniedException("Filesystem is read-only");
				default:
					throw new UnsupportedOperationException("Unsupported mode: " + mode);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V extends FileAttributeView> V getFileAttributeView(Path path,
		Class<V> type, LinkOption... options)
	{
		Preconditions.checkNotNull(path, "path may not be null");
		Preconditions.checkNotNull(type, "type may not be null");
		Preconditions.checkNotNull(options, "options may not be null");
		if (!BasicFileAttributeView.class.isAssignableFrom(type))
			return null;
		// Ignore "options" because we don't support symbolic links
		return (V) new ReleaseFileAttributeView((ReleasePath) path);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends BasicFileAttributes> A readAttributes(Path path,
		Class<A> type, LinkOption... options) throws IOException
	{
		Preconditions.checkNotNull(type, "type may not be null");
		if (!type.equals(BasicFileAttributes.class))
			throw new UnsupportedOperationException("Unsupported attribute type: " + type);
		return (A) getFileAttributeView(path, BasicFileAttributeView.class, options).readAttributes();
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
		throws IOException
	{
		int separator = attributes.indexOf(':');
		if (separator != -1)
		{
			String viewName = attributes.substring(0, separator);
			if (!viewName.equals("basic"))
				throw new UnsupportedOperationException(viewName + " not supported");
		}
		Set<String> keys = Sets.newHashSet(Splitter.on(',').
			split(attributes.substring(separator + 1)));
		if (keys.contains("*"))
		{
			keys.add("lastModifiedTime");
			keys.add("lastAccessTime");
			keys.add("creationTime");
			keys.add("size");
			keys.add("isRegularFile");
			keys.add("isDirectory");
			keys.add("isSymbolicLink");
			keys.add("isOther");
			keys.add("fileKey");
		}
		if (keys.isEmpty())
			throw new IllegalArgumentException("Attributes may not be null");
		BasicFileAttributes basicAttributes = readAttributes(path, BasicFileAttributes.class, options);
		Map<String, Object> result = new HashMap<>();
		for (String key : keys)
		{
			switch (key)
			{
				case "lastModifiedTime":
				{
					result.put(key, basicAttributes.lastModifiedTime());
					break;
				}
				case "lastAccessTime":
				{
					result.put(key, basicAttributes.lastAccessTime());
					break;
				}
				case "creationTime":
				{
					result.put(key, basicAttributes.creationTime());
					break;
				}
				case "size":
				{
					result.put(key, basicAttributes.size());
					break;
				}
				case "isRegularFile":
				{
					result.put(key, basicAttributes.isRegularFile());
					break;
				}
				case "isDirectory":
				{
					result.put(key, basicAttributes.isDirectory());
					break;
				}
				case "isSymbolicLink":
				{
					result.put(key, basicAttributes.isSymbolicLink());
					break;
				}
				case "isOther":
				{
					result.put(key, basicAttributes.isOther());
					break;
				}
				case "fileKey":
				{
					result.put(key, basicAttributes.isOther());
					break;
				}
				default:
					throw new IllegalArgumentException("Unrecognized attribute: " + key);
			}
		}
		return result;
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws
		IOException
	{
		throw new ReadOnlyFileSystemException();
	}
}
