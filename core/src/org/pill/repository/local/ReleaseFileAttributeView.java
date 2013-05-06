package org.pill.repository.local;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import org.pill.repository.Repository;

/**
 * An attribute view of files belonging to a Release.
 * <p/>
 * THREAD-SAFETY: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
class ReleaseFileAttributeView implements BasicFileAttributeView
{
	private final ReleasePath path;

	/**
	 * Creates a new ReleaseFileAttributeView.
	 * <p/>
	 * @param path the path associated with the view
	 * @throws NullPointerException if path is null
	 */
	public ReleaseFileAttributeView(ReleasePath path)
	{
		Preconditions.checkNotNull(path, "path may not be null");

		this.path = path;
	}

	@Override
	public String name()
	{
		return "basic";
	}

	@Override
	public BasicFileAttributes readAttributes() throws IOException
	{
		ReleaseFileSystem filesystem = path.getFileSystem();
		Repository repository = filesystem.getRepository();
		return repository.readAttributes(path.toUri());
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime)
		throws IOException
	{
		if (path.getFileSystem().isReadOnly())
			throw new ReadOnlyFileSystemException();
		FileTime existingValue = readAttributes().lastModifiedTime();
		if (lastModifiedTime != null && !lastModifiedTime.equals(existingValue))
		{
			throw new IOException("lastModifiedTime may not be modified. Current value: "
				+ existingValue + ", new value: " + lastModifiedTime);
		}
		if (lastAccessTime != null && !lastAccessTime.equals(existingValue))
		{
			throw new IOException("lastAccessTime may not be modified. Current value: "
				+ existingValue + ", new value: " + lastAccessTime);
		}
		if (createTime != null && !createTime.equals(existingValue))
		{
			throw new IOException("createTime may not be modified. Current value: "
				+ existingValue + ", new value: " + createTime);
		}
	}
}
