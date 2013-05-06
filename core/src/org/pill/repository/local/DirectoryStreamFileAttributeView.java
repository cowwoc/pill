package org.pill.repository.local;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import org.pill.repository.local.ReleaseDirectoryStream;
import org.pill.repository.local.ReleaseFileAttributeView;
import org.pill.repository.local.ReleasePath;

/**
 * An attribute view of files belonging to a Release.
 * <p/>
 * THREAD-SAFETY: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
final class DirectoryStreamFileAttributeView extends ReleaseFileAttributeView
{
	private final ReleaseDirectoryStream directoryStream;

	/**
	 * Creates a new ReleaseFileAttributeView.
	 * <p/>
	 * @param directoryStream the ReleaseDirectoryStream associated with the view
	 * @param directory the directory associated with the view
	 * @throws NullPointerException if directoryStream or directory are null
	 */
	public DirectoryStreamFileAttributeView(ReleaseDirectoryStream directoryStream,
		ReleasePath directory)
	{
		super(directory);
		Preconditions.checkNotNull(directoryStream, "directoryStream may not be null");

		this.directoryStream = directoryStream;
	}

	@Override
	public BasicFileAttributes readAttributes() throws IOException
	{
		if (!directoryStream.isOpen())
			throw new ClosedDirectoryStreamException();
		return super.readAttributes();
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime)
		throws IOException
	{
		if (!directoryStream.isOpen())
			throw new ClosedDirectoryStreamException();
		super.setTimes(lastModifiedTime, lastAccessTime, createTime);
	}
}
