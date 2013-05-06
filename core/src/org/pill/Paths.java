package org.pill;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Path helper functions.
 * <p/>
 * @author Gili Tzabari
 */
public class Paths
{
	private static final int MAX_RETRY = 3;

	/**
	 * Deletes a path and its descendants recursively, if it exists.
	 * <p/>
	 * @param path the directory to delete
	 * @throws NotDirectoryException if the file could not otherwise be opened because it is not a
	 * directory <i>(optional specific exception)</i>
	 * @throws IOException if the path is not a directory or if the underlying filesystem does not
	 * support deleting paths in a race-free manner
	 */
	@SuppressWarnings("SleepWhileInLoop")
	public static void deleteRecursively(Path path) throws IOException, NotDirectoryException
	{
		Logger log = LoggerFactory.getLogger(Paths.class);

		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
		{
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(path))
			{
				if (ds instanceof SecureDirectoryStream)
					deleteDescendants((SecureDirectoryStream<Path>) ds);
				else
				{
					log.debug("The file-system does not support deleting paths in a race-free manner");
					deleteDescendants(ds);
				}
			}
		}
		int retry = 0;
		while (true)
		{
			try
			{
				Files.deleteIfExists(path);
				break;
			}
			catch (DirectoryNotEmptyException e)
			{
				// Perhaps the directory is locked, retry...
				++retry;
				if (retry == MAX_RETRY)
					throw e;
				try
				{
					Thread.sleep(300);
				}
				catch (InterruptedException unused)
				{
					break;
				}
			}
		}
	}

	/**
	 * Deletes a directory and its descendants.
	 * <p/>
	 * @param directory the directory stream
	 * @throws NotDirectoryException if a child directory changes into a file in mid-operation
	 * <i>(optional specific exception)</i>
	 * @throws IOException if an I/O error occurs while deleting the descendants
	 */
	private static void deleteDescendants(DirectoryStream<Path> directory)
		throws IOException
	{
		for (Path child : directory)
		{
			if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS))
			{
				try (DirectoryStream<Path> childDirectory = Files.newDirectoryStream(child))
				{
					deleteDescendants(childDirectory);
				}
			}
			Files.deleteIfExists(child);
		}
	}

	/**
	 * Deletes a directory and its descendants.
	 * <p/>
	 * @param directory the directory stream
	 * @throws NotDirectoryException if a child directory changes into a file in mid-operation
	 * <i>(optional specific exception)</i>
	 * @throws IOException if an I/O error occurs while deleting the descendants
	 */
	private static void deleteDescendants(SecureDirectoryStream<Path> directory)
		throws IOException
	{
		for (Path child : directory)
		{
			if (directory.getFileAttributeView(child, BasicFileAttributeView.class,
				LinkOption.NOFOLLOW_LINKS).readAttributes().isDirectory())
			{
				try (SecureDirectoryStream<Path> childDirectory =
					directory.newDirectoryStream(child, LinkOption.NOFOLLOW_LINKS))
				{
					deleteDescendants(childDirectory);
				}
				directory.deleteDirectory(child);
			}
			else
				directory.deleteDirectory(child);
		}
		directory.deleteDirectory(java.nio.file.Paths.get("."));
	}
}
