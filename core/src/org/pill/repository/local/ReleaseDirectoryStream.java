package org.pill.repository.local;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A directory stream for a Release.
 * <p/>
 * THREAD-SAFETY: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
final class ReleaseDirectoryStream implements SecureDirectoryStream<Path>
{
	private final ReleaseFileSystemProvider provider;
	private final ReleasePath directory;
	private final Filter<? super Path> filter;
	private final Logger log = LoggerFactory.getLogger(ReleaseDirectoryStream.class);
	private final AtomicBoolean iterated = new AtomicBoolean();
	private final AtomicBoolean closed = new AtomicBoolean();

	/**
	 * Creates a new ReleaseDirectoryStream.
	 * <p/>
	 * @param provider the filesystem provider
	 * @param dir the directory being browsed
	 * @param filter the filter applied to the stream
	 * @throws NullPointerException if provider, dir or filter are null
	 */
	public ReleaseDirectoryStream(ReleaseFileSystemProvider provider, ReleasePath dir,
		Filter<? super Path> filter)
	{
		Preconditions.checkNotNull(provider, "provider may not be null");
		Preconditions.checkNotNull(dir, "dir may not be null");
		Preconditions.checkNotNull(filter, "filter may not be null");

		this.provider = provider;
		this.directory = dir;
		this.filter = filter;
	}

	@Override
	public SecureDirectoryStream<Path> newDirectoryStream(Path path, LinkOption... options) throws
		IOException
	{
		Preconditions.checkNotNull(path, "path may not be null");
		Preconditions.checkNotNull(options, "options may not be null");
		if (closed.get())
			throw new ClosedDirectoryStreamException();

		// Ignore "options" because we don't support symbolic links
		Path absolute = directory.resolve(path);
		return provider.newDirectoryStream(absolute, filter);
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
		FileAttribute<?>... attrs) throws IOException
	{
		Preconditions.checkNotNull(path, "path may not be null");
		Preconditions.checkNotNull(options, "options may not be null");
		Preconditions.checkNotNull(attrs, "attrs may not be null");
		if (closed.get())
			throw new ClosedDirectoryStreamException();
		if (directory.getFileSystem().isReadOnly() && options.contains(StandardOpenOption.WRITE)
			|| options.contains(StandardOpenOption.DELETE_ON_CLOSE))
		{
			throw new ReadOnlyFileSystemException();
		}
		Path absolute = directory.resolve(path);
		return provider.newByteChannel(absolute, options, attrs);
	}

	@Override
	public void deleteFile(Path path) throws IOException
	{
		Preconditions.checkNotNull(path, "path may not be null");
		if (closed.get())
			throw new ClosedDirectoryStreamException();
		if (directory.getFileSystem().isReadOnly())
			throw new ReadOnlyFileSystemException();
		Path absolute = directory.resolve(path);
		if (absolute.equals(absolute.getRoot()))
		{
			log.warn("Skipping directory: " + absolute);
			return;
		}
		provider.delete(absolute);
	}

	@Override
	public void deleteDirectory(Path path) throws IOException
	{
		Preconditions.checkNotNull(path, "path may not be null");
		if (closed.get())
			throw new ClosedDirectoryStreamException();
		if (directory.getFileSystem().isReadOnly())
			throw new ReadOnlyFileSystemException();
		Path absolute = directory.resolve(path);
		if (!absolute.equals(absolute.getRoot()))
		{
			log.warn("Skipping file: " + absolute);
			return;
		}
		provider.delete(absolute);
	}

	@Override
	public void move(Path srcpath,
		SecureDirectoryStream<Path> targetdir, Path targetpath) throws IOException
	{
		Preconditions.checkNotNull(srcpath, "srcpath may not be null");
		Preconditions.checkNotNull(targetdir, "targetdir may not be null");
		Preconditions.checkNotNull(targetpath, "targetpath may not be null");
		if (closed.get())
			throw new ClosedDirectoryStreamException();
		if (directory.getFileSystem().isReadOnly())
			throw new ReadOnlyFileSystemException();
		if (!(targetdir instanceof ReleaseDirectoryStream))
		{
			throw new ProviderMismatchException("targetdir's was of type "
				+ targetdir.getClass().getName() + " instead of " + getClass().getName());
		}
		throw new ReadOnlyFileSystemException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V extends FileAttributeView> V getFileAttributeView(Class<V> type)
	{
		Preconditions.checkNotNull(type, "type may not be null");
		if (!BasicFileAttributeView.class.isAssignableFrom(type))
			return null;
		return (V) new DirectoryStreamFileAttributeView(this, directory);
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
		return (V) new DirectoryStreamFileAttributeView(this, (ReleasePath) path);
	}

	@Override
	public Iterator<Path> iterator()
	{
		if (!iterated.compareAndSet(false, true))
			throw new IllegalStateException("iterator() was already invoked");
		ReleaseFileSystem filesystem = directory.getFileSystem();
		if (!filesystem.isOpen())
			throw new ClosedFileSystemException();
		return Iterators.singletonIterator(provider.getPath(directory.toUri()));
	}

	@Override
	public void close() throws IOException
	{
		closed.set(true);
	}

	/**
	 * @return true if the directory stream is open
	 */
	public boolean isOpen()
	{
		return !closed.get();
	}
}
