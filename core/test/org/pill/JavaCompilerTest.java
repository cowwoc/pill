package org.pill;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.pill.repository.Repository;
import org.pill.repository.local.LocalRepository;
import org.testng.annotations.Test;

/**
 * @author Gili Tzabari
 */
public class JavaCompilerTest
{
	@Test
	public void classpathUsed() throws IOException, CompilationException
	{
		JavaCompiler compiler = new JavaCompiler().debug(JavaCompiler.DebugType.LINES,
			JavaCompiler.DebugType.SOURCE, JavaCompiler.DebugType.VARIABLES);
		Path projectPath = Modules.getRootPath(JavaCompilerTest.class).getParent().getParent().
			getParent();
		Repository localRepository = LocalRepository.getInstance();

		URI guavaReleaseUri;
		Module guavaModule;
		String guavaVersion = "11.0.1";
		try
		{
			guavaModule = localRepository.getModule("com.google.common");
			if (guavaModule == null)
				guavaModule = localRepository.insertModule("com.google.common");
			guavaReleaseUri = localRepository.getReleaseUri(guavaModule, guavaVersion);
			if (guavaReleaseUri == null)
			{
				guavaReleaseUri = localRepository.insertRelease(guavaModule, guavaVersion,
					projectPath.resolve("lib/guava/guava-" + guavaVersion + ".jar")).build().getUri();
			}
		}
		catch (EntityExistsException e)
		{
			throw new AssertionError(e);
		}

		Path buildPath = projectPath.resolve("build/test");
		Path sourcePath = projectPath.resolve("test");
		List<Path> sourceFiles = ImmutableList.of(sourcePath.resolve(
			Paths.get(DependsOnGuava.class.getName().replace('.', '/') + ".java")));
		Files.createDirectories(buildPath);
		Path dependenciesPath = buildPath.resolve("dependencies");
		Files.createDirectories(dependenciesPath);

		Dependency guavaDependency = new Dependency(guavaReleaseUri, guavaModule, guavaVersion,
			DependencyType.BUILD);

		// Test navigating from a dependency to a release
		guavaReleaseUri = localRepository.getReleaseUri(guavaDependency.getModule(),
			guavaDependency.getVersion());
		assert (guavaReleaseUri != null);
		Release guavaRelease = localRepository.getRelease(guavaReleaseUri);
		assert (guavaRelease != null);
		guavaRelease.copyTo(dependenciesPath, StandardCopyOption.REPLACE_EXISTING);
		List<Path> classpath = new ArrayList<>();
		classpath.add(dependenciesPath.resolve(Paths.get(guavaReleaseUri).toString()));

		compiler.sourcePath(ImmutableList.of(sourcePath)).classPath(classpath);
		compiler.run(sourceFiles, buildPath);
	}

	@Test
	public void sourcesUsed() throws IOException, CompilationException
	{
	}
}
