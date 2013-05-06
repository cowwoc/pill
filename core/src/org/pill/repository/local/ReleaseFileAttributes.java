package org.pill.repository.local;

import com.google.common.base.Preconditions;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * The file attributes of a file belonging to a Release.
 * <p/>
 * THREAD-SAFETY: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
final class ReleaseFileAttributes implements BasicFileAttributes
{
	private final String path;
	private final long size;
	private final FileTime lastModified;

	/**
	 * Creates a new ReleaseFileAttributes.
	 * <p/>
	 * @param path the path associated with the file attributes
	 * @param size the path size
	 * @param lastModified the last time the file was modified
	 * @throws NullPointerException if path is null
	 */
	public ReleaseFileAttributes(String path, long size, FileTime lastModified)
	{
		Preconditions.checkNotNull(path, "path may not be null");
		Preconditions.checkNotNull(lastModified, "lastModified may not be null");

		this.path = path;
		this.size = size;
		this.lastModified = lastModified;
	}

	@Override
	public FileTime lastModifiedTime()
	{
		return lastModified;
	}

	@Override
	public FileTime lastAccessTime()
	{
		return lastModified;
	}

	@Override
	public FileTime creationTime()
	{
		return lastModified;
	}

	@Override
	public boolean isRegularFile()
	{
		return !path.equals("/");
	}

	@Override
	public boolean isDirectory()
	{
		return !isRegularFile();
	}

	@Override
	public boolean isSymbolicLink()
	{
		return false;
	}

	@Override
	public boolean isOther()
	{
		return false;
	}

	@Override
	public long size()
	{
		return size;
	}

	@Override
	public Object fileKey()
	{
		return path;
	}
}
