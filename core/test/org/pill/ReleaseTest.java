package org.pill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import org.pill.repository.Repository;
import org.pill.repository.local.LocalRepository;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gili Tzabari
 */
public class ReleaseTest
{
	@Test(expectedExceptions = NoSuchFileException.class)
	public void copyToNonexistentTarget() throws EntityExistsException, IOException
	{
		Repository localRepository = LocalRepository.getInstance();
		Module module = localRepository.insertModule("com.google.common");
		Path source = Files.createTempFile(null, null);
		Files.write(source, new byte[]
		{
			1, 2, 3
		}, StandardOpenOption.WRITE);
		Release release = localRepository.insertRelease(module, "1.0", source).build();
		Path target = Files.createTempFile(null, null);
		Files.delete(target);
		release.copyTo(target);
	}

	@Test(expectedExceptions = NoSuchFileException.class)
	public void copyToExistentFile() throws EntityExistsException, IOException
	{
		Repository localRepository = LocalRepository.getInstance();
		Module module = localRepository.insertModule("com.google.common");
		Path source = Files.createTempFile(null, null);
		Files.write(source, new byte[]
		{
			1, 2, 3
		}, StandardOpenOption.WRITE);
		Release release = localRepository.insertRelease(module, "1.0", source).build();
		Path target = Files.createTempFile(null, null);
		release.copyTo(target);
	}

	/**
	 * Import a file into the repository, then overwrite it using the copy in the repository.
	 */
	@Test
	public void copyToSource() throws EntityExistsException, IOException
	{
		Repository localRepository = LocalRepository.getInstance();
		Module module = localRepository.insertModule("com.google.common");
		Path source = Files.createTempFile(null, null);
		Files.write(source, new byte[]
		{
			1, 2, 3
		}, StandardOpenOption.WRITE);
		Release release = localRepository.insertRelease(module, "1.0", source).build();
		Files.write(source, new byte[]
		{
			4, 5, 6
		}, StandardOpenOption.WRITE);
		release.copyTo(source.getParent(), StandardCopyOption.REPLACE_EXISTING);
		Assert.assertEquals(Files.readAllBytes(source),
			new byte[]
		{
			1, 2, 3
		});
	}

	/**
	 * Skips files with a newer last-modified-time than the source.
	 */
	@Test
	public void copyToSkipNewer() throws EntityExistsException, IOException
	{
		Repository localRepository = LocalRepository.getInstance();
		Module module = localRepository.insertModule("com.google.common");
		Path source = Files.createTempFile(null, null);
		Files.write(source, new byte[]
		{
			1, 2, 3
		}, StandardOpenOption.WRITE);
		Release release = localRepository.insertRelease(module, "1.0", source).build();
		Files.write(source, new byte[]
		{
			4, 5, 6
		}, StandardOpenOption.WRITE);
		release.copyTo(source.getParent(), StandardCopyOption.REPLACE_EXISTING,
			PerformanceCopyOption.SKIP_NEWER);
		Assert.assertEquals(Files.readAllBytes(source),
			new byte[]
		{
			4, 5, 6
		});
	}
}
