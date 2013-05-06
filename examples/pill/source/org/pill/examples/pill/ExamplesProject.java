package org.pill.examples.pill;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.pill.CompilationException;
import org.pill.Dependency;
import org.pill.DependencyType;
import org.pill.EntityExistsException;
import org.pill.JavaCompiler;
import org.pill.JavaCompiler.DebugType;
import org.pill.Module;
import org.pill.Modules;
import org.pill.Release;
import org.pill.repository.Repository;
import org.pill.repository.local.LocalRepository;

/**
 * Builds the examples project.
 * <p/>
 * @author Gili Tzabari
 */
public class ExamplesProject
{
	/**
	 * @param args the command line arguments
	 * <p/>
	 * @throws IOException if an I/O error occurs
	 * @throws CompilationException if an error occurs compiling the project
	 */
	public static void main(String[] args)
		throws IOException, CompilationException
	{
		Repository localRepository = LocalRepository.getInstance();

		Path scriptPath = Modules.getRootPath(ExamplesProject.class).getParent().getParent();
		URI guavaReleaseUri;
		Module guavaModule = localRepository.getModule("com.google.common");
		String guavaVersion = "11.0.1";
		try
		{
			if (guavaModule == null)
				guavaModule = localRepository.insertModule("com.google.common");
			guavaReleaseUri = localRepository.getReleaseUri(guavaModule, guavaVersion);
			if (guavaReleaseUri == null)
			{
				guavaReleaseUri = localRepository.insertRelease(guavaModule, guavaVersion,
					scriptPath.resolve("lib/guava/guava-11.0.1.jar")).build().getUri();
			}
		}
		catch (EntityExistsException e)
		{
			throw new AssertionError(e);
		}

		Dependency guavaDependency = new Dependency(guavaReleaseUri, guavaModule, guavaVersion,
			DependencyType.BUILD);
		guavaReleaseUri = localRepository.getRelease(guavaDependency.getUri()).getUri();

		Path projectPath = scriptPath.getParent();
		Path buildPath = projectPath.resolve("build");
		Files.createDirectories(buildPath);
		Path dependenciesPath = buildPath.resolve("dependencies");
		Files.createDirectories(dependenciesPath);
		Release guavaRelease = localRepository.getRelease(guavaReleaseUri);
		assert (guavaRelease != null);
		guavaRelease.copyTo(dependenciesPath, StandardCopyOption.REPLACE_EXISTING);
		List<Path> classpath = new ArrayList<>();
		classpath.add(dependenciesPath.resolve(Paths.get(guavaReleaseUri).toString()));

		final List<Path> sourceFiles = new ArrayList<>();
		final Path sourcePath = projectPath.resolve("src");
		Files.walkFileTree(sourcePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
			Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				if (file.toString().endsWith(".java"))
					sourceFiles.add(file);
				return FileVisitResult.CONTINUE;
			}
		});

		JavaCompiler compiler = new JavaCompiler().
			debug(DebugType.LINES, DebugType.SOURCE, DebugType.VARIABLES).
			sourcePath(Collections.singletonList(projectPath)).
			classPath(classpath);
		compiler.run(sourceFiles, buildPath);
	}
}
