package org.pill.repository.local;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.pill.Dependency;
import org.pill.DependencyType;
import org.pill.EntityExistsException;
import org.pill.EntityNotFoundException;
import org.pill.Module;
import org.pill.Release;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * @author Gili Tzabari
 */
public class LocalRepositoryTest
{
	private final String packageName = LocalRepository.class.getPackage().getName();

	@BeforeSuite
	public static void beforeSuite()
	{
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		for (Handler handler : rootLogger.getHandlers())
			rootLogger.removeHandler(handler);
		SLF4JBridgeHandler.install();
	}

	@AfterSuite
	public static void afterSuite()
	{
		SLF4JBridgeHandler.uninstall();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void insertModuleNullName() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		localrepository.insertModule(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void insertModuleEmptyName() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		localrepository.insertModule("");
	}

	@Test
	public void insertModule() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Assert.assertNotNull(module);
	}

	@Test(expectedExceptions = EntityExistsException.class)
	public void insertExistingModule() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Assert.assertNotNull(module);

		localrepository.insertModule("com.google.common");
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void insertReleaseNullVersion() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		localrepository.insertRelease(module, null, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void insertReleaseEmptyVersion() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		localrepository.insertRelease(module, "", null);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void insertReleaseNullFile() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		localrepository.insertRelease(module, "1.0", null);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void insertReleaseNullDependency() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).addDependency(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void insertReleaseMissingModule()
		throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		localrepository.removeModule(module);
		localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).build();
	}

	@Test
	public void pathToFile() throws URISyntaxException
	{
		String expected = packageName + ".release:1:file.jar";
		Path path = new ReleaseFileSystemProvider().getPath(new URI(expected));
		Assert.assertEquals(path.toUri().toString(), expected);
	}

	@Test
	public void insertRelease() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Release release = localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).build();
		Assert.assertNotNull(release);
		Assert.assertEquals(release.getUri().toString(), packageName + ".release:1");

		Path file = java.nio.file.Paths.get(release.getUri());
		Assert.assertEquals(file.toUri().toString(),
			release.getUri() + ":" + file.getFileName().toString());
	}

	@Test
	public void insertDependency() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module guavaModule = localrepository.insertModule("com.google.common");
		String guavaVersion = "11.0.1";
		Release guavaRelease = localrepository.insertRelease(guavaModule, guavaVersion,
			Paths.get("release.jar")).build();
		Assert.assertNotNull(guavaRelease);
		Assert.assertEquals(guavaRelease.getUri().toString(), packageName + ".release:1");

		Dependency guavaDependency = new Dependency(guavaRelease.getUri(),
			guavaModule, guavaVersion, DependencyType.BUILD);
		Assert.assertEquals(guavaDependency.getUri(), guavaRelease.getUri());

		Module exampleModule = localrepository.insertModule("org.pill.example");
		Release exampleRelease = localrepository.insertRelease(exampleModule, "1.0",
			Paths.get("release.jar")).addDependency(guavaDependency).build();
		Assert.assertNotNull(exampleRelease);
		Assert.assertEquals(exampleRelease.getUri().toString(), packageName + ".release:2");

		Release guavaRelease2 = localrepository.getRelease(guavaDependency.getUri());
		Assert.assertEquals(guavaRelease2, guavaRelease);
	}

	@Test(expectedExceptions = EntityExistsException.class)
	public void insertExistingRelease() throws EntityExistsException, IOException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Assert.assertNotNull(module);

		localrepository.insertModule("com.google.common");
		String version = "1.0";
		Release release = localrepository.insertRelease(module, version, Paths.get("release.jar")).
			build();
		localrepository.insertRelease(module, version, Paths.get(release.getUri())).build();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void removeNullRelease()
		throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		localrepository.removeRelease(null);
	}

	@Test
	public void removeRelease() throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Release release = localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).build();
		localrepository.removeRelease(release);
	}

	@Test(expectedExceptions = EntityNotFoundException.class)
	public void removeNonexistentRelease()
		throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Release release = localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).build();
		try
		{
			localrepository.removeRelease(release);
		}
		catch (EntityNotFoundException e)
		{
			Assert.fail("removeRelease() threw EntityNotFoundException unexpectedly", e);
		}
		localrepository.removeRelease(release);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void removeNullModule()
		throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		localrepository.removeModule(null);
	}

	@Test
	public void removeModule() throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Release release = localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).build();
		localrepository.removeRelease(release);
		localrepository.removeModule(module);
	}

	@Test(expectedExceptions = EntityNotFoundException.class)
	public void removeNonexistentModule()
		throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		Release release = localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).build();
		try
		{
			localrepository.removeRelease(release);
			localrepository.removeModule(module);
		}
		catch (EntityNotFoundException e)
		{
			Assert.fail("removeRelease() or removeModule() threw EntityNotFoundException unexpectedly", e);
		}
		localrepository.removeModule(module);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void removeNonEmptyModule()
		throws EntityExistsException, IOException, EntityNotFoundException
	{
		LocalRepository localrepository = LocalRepository.getInstance();
		Module module = localrepository.insertModule("com.google.common");
		localrepository.insertRelease(module, "1.0", Paths.get("release.jar")).build();
		localrepository.removeModule(module);
	}
}
