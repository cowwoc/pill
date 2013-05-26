package org.pill;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;
import org.pill.repository.Repository;
import org.pill.repository.local.ClassloaderBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a pill script.
 * <p/>
 * @author Gili Tzabari
 */
public class ScriptBuilder
{
	private final Path scriptPath;
	private List<Path> classPath = ImmutableList.of();
	private final Logger log = LoggerFactory.getLogger(ScriptBuilder.class);

	/**
	 * Creates a new ScriptBuilder.
	 * <p/>
	 * @param scriptPath the script path
	 * @throws NullPointerException if scriptPath is null
	 */
	public ScriptBuilder(Path scriptPath)
	{
		Preconditions.checkNotNull(scriptPath, "scriptPath may not be null");

		this.scriptPath = scriptPath;
	}

	/**
	 * Sets the compiler classpath.
	 * <p/>
	 * @param classPath the paths to search for the compiled format of dependent classes
	 * @throws NullPointerException if classPath is null
	 * @throws IllegalArgumentException if classPath refers to a non-existent path
	 * @return the ScriptBuilder
	 */
	public ScriptBuilder classPath(List<Path> classPath)
	{
		Preconditions.checkNotNull(classPath, "classPath may not be null");
		for (Path path: classPath)
		{
			if (!Files.exists(path))
			{
				throw new IllegalArgumentException("classPath refers to non-existant path: " + path.
					toAbsolutePath());
			}
		}

		this.classPath = ImmutableList.copyOf(classPath);
		return this;
	}

	/**
	 * Builds the project.
	 * <p/>
	 * @throws FileNotFoundException if the project is missing a manifest file
	 * @throws IOException if an I/O errors occurs
	 * @throws CompilationException if an error occurs while compiling the build script
	 */
	public void run() throws FileNotFoundException, IOException, CompilationException
	{
		Path sourcePath = scriptPath.resolve("source");
		Path targetPath = scriptPath.resolve("target/classes");
		compileProjectBuilder(sourcePath, targetPath);
		Path logbackSource = sourcePath.resolve("logback.xml");

		ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
		Collection<String> systemClasses = ImmutableList.of("sun.", "java.", "javax.", "org.omg.",
			"org.w3c.dom.", "org.xml.sax.", ClassloaderBridge.class.getName(), Repository.class.getName(),
			Release.class.getName());

		if (Files.exists(logbackSource))
		{
			// WORKAROUND: http://jira.qos.ch/browse/LOGBACK-857
			LocalClassLoader pillClassLoader = new LocalClassLoader(parentClassLoader);

			Path logbackTarget = scriptPath.resolve("target/logback/logback.xml");
			Files.createDirectories(logbackTarget.getParent());
			Files.copy(logbackSource, logbackTarget, StandardCopyOption.REPLACE_EXISTING);
			pillClassLoader.addURL(logbackTarget.getParent().toUri().toURL());

			Pill pill = new Pill();
			for (Path path: pill.getClassPath())
				pillClassLoader.addURL(path.toUri().toURL());
			pillClassLoader.hiddenLocalResources().add(parentClassLoader.getResource("logback.xml"));
			pillClassLoader.inheritedClasses().addAll(systemClasses);
			parentClassLoader = pillClassLoader;
		}
		LocalClassLoader scriptClassLoader = new LocalClassLoader(parentClassLoader);
		scriptClassLoader.addURL(targetPath.toUri().toURL());
		scriptClassLoader.inheritedClasses().addAll(systemClasses);
		scriptClassLoader.inheritedClasses().add("org.pill.");
		scriptClassLoader.inheritedResources().add("");

		Thread.currentThread().setContextClassLoader(scriptClassLoader);
		ClassloaderBridge.create(scriptClassLoader);
		Method mainMethod;
		try
		{
			mainMethod = getMainMethod(sourcePath, scriptClassLoader);
		}
		catch (ClassNotFoundException e)
		{
			// We just compiled the source-code, so the file should exist. Perhaps someone deleted it?
			throw new IOException(e);
		}
		log.debug("Running {}.main(String[]) with classpath {}", mainMethod.getDeclaringClass(),
			targetPath);
		try
		{
			mainMethod.invoke(null, new Object[]
			{
				new String[0]
			});
		}
		catch (ReflectiveOperationException e)
		{
			throw new IOException("Exception while running " + mainMethod.getDeclaringClass()
				+ ".main(String[])", e);
		}
	}

	/**
	 * Returns the main(String[]) method of the Main-Class associated with a source-code directory.
	 * <p/>
	 * @param sourcePath the source-code path
	 * @param classLoader the ClassLoader used to load the class
	 * @return null if the manifest does not contain Main-Class
	 * @throws NullPointerException if scriptPath or cl are null
	 * @throws IOException if an I/O error occurs while reading the manifest
	 * @throws ClassNotFoundException if the main class cannot be found
	 */
	private Method getMainMethod(Path sourcePath, ClassLoader cl)
		throws IOException, ClassNotFoundException
	{
		Preconditions.checkNotNull(sourcePath, "sourcePath may not be null");
		Preconditions.checkNotNull(cl, "cl may not be null");

		Path manifestPath = sourcePath.resolve("META-INF/MANIFEST.MF");
		String mainClassName;
		try (InputStream in = Files.newInputStream(manifestPath))
		{
			Manifest manifest = new Manifest(in);
			mainClassName = manifest.getMainAttributes().getValue("Main-Class");
			if (mainClassName == null)
				throw new IOException(manifestPath.toAbsolutePath() + " is missing Main-Class");
		}
		log.debug("mainClass: {}", mainClassName);
		Class<?> mainClass = Class.forName(mainClassName, true, cl);
		try
		{
			return mainClass.getDeclaredMethod("main", new Class<?>[]
			{
				String[].class
			});
		}
		catch (NoSuchMethodException e)
		{
			throw new IOException("Exception while looking up " + mainClass + ".main(String[]) method", e);
		}
	}

	/**
	 * Compiles the build script.
	 * <p/>
	 * @param sourcePath the directory containing the source-code of the build script
	 * @param targetPath the directory to compile into
	 * @throws IOException if an I/O errors occurs
	 * @throws CompilationException if an error occurs while compiling the build script
	 */
	private void compileProjectBuilder(Path sourcePath, Path targetPath)
		throws CompilationException, IOException
	{
		log.debug("Compiling source files");
		org.pill.Paths.deleteRecursively(targetPath);
		final Collection<Path> sourceFiles = new ArrayList<>();
		final Collection<Path> resourceFiles = new ArrayList<>();
		Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				if (file.toString().endsWith(".java"))
					sourceFiles.add(file);
				else
					resourceFiles.add(file);
				return super.visitFile(file, attrs);
			}
		});
		Files.createDirectories(targetPath);

		new JavaCompiler().classPath(classPath).run(sourceFiles, targetPath);
		for (Path path: resourceFiles)
		{
			Path relativePath = sourcePath.relativize(path);
			Path targetFile = targetPath.resolve(relativePath);
			Files.createDirectories(targetFile.getParent());
			Files.copy(path, targetFile, StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES);
		}
	}
}
