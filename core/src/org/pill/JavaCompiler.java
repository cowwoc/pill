package org.pill;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.sun.xml.internal.rngom.ast.builder.BuildException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles Java source-code.
 * <p/>
 * @author Gili Tzabari
 */
public final class JavaCompiler
{
	private List<Path> sourcePath = new ArrayList<>();
	private List<Path> classPath = ImmutableList.of();
	private final Set<DebugType> debugOptions = new HashSet<>(Arrays.asList(DebugType.LINES,
		DebugType.SOURCE, DebugType.VARIABLES));

	/**
	 * Sets the compiler classpath.
	 * <p/>
	 * @param sourcePath the paths to search for the source format of dependent classes
	 * @throws NullPointerException if sourcePath is null
	 * @throws IllegalArgumentException if sourcePath refers to a non-existent path or a non-directory
	 * @return the JavaCompiler
	 */
	public JavaCompiler sourcePath(List<Path> sourcePath)
	{
		Preconditions.checkNotNull(sourcePath, "sourcePath may not be null");
		for (Path path : sourcePath)
		{
			if (!Files.exists(path))
			{
				throw new IllegalArgumentException("sourcePath refers to non-existant path: " + path.
					toAbsolutePath());
			}
			if (!Files.isDirectory(path))
			{
				throw new IllegalArgumentException("sourcePath refers to a non-directory: " + path.
					toAbsolutePath());
			}
		}

		this.sourcePath = ImmutableList.copyOf(sourcePath);
		return this;
	}

	/**
	 * Sets the compiler classpath.
	 * <p/>
	 * @param classPath the paths to search for the compiled format of dependent classes
	 * @throws NullPointerException if classPath is null
	 * @throws IllegalArgumentException if classPath refers to a non-existent path
	 * @return the JavaCompiler
	 */
	public JavaCompiler classPath(List<Path> classPath)
	{
		Preconditions.checkNotNull(classPath, "classPath may not be null");
		for (Path path : classPath)
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
	 * Indicates the kind of debugging information the generated files should contain.
	 * <p/>
	 * @param debugOptions the kind of debugging information the generated files should contain. By
	 * default all debugging information is generated.
	 * @return the JavaCompiler object
	 */
	public JavaCompiler debug(DebugType... debugOptions)
	{
		this.debugOptions.clear();
		this.debugOptions.addAll(Arrays.asList(debugOptions));
		return this;
	}

	/**
	 * Compiles the source code.
	 * <p/>
	 * @param sourceFiles the source files to compile
	 * @param targetDirectory the directory to compile into. This path included in the compiler
	 * classpath.
	 * @throws IllegalArgumentException if sourceFiles, targetDirectory are null; or if sourceFiles
	 * refers to a non-existent file or a non-file; or if targetDirectory is not a directory
	 * @throws CompilationException if the operation fails
	 */
	public void run(final Collection<Path> sourceFiles, final Path targetDirectory)
		throws IllegalArgumentException, CompilationException
	{
		if (sourceFiles == null)
			throw new IllegalArgumentException("sourceFiles may not be null");
		if (sourceFiles.isEmpty())
			return;
		for (Path file : sourceFiles)
		{
			if (!Files.exists(file))
			{
				throw new IllegalArgumentException("sourceFiles refers to a non-existant file: "
					+ file.toAbsolutePath());
			}
			if (!Files.isRegularFile(file))
			{
				throw new IllegalArgumentException("sourceFiles refers to a non-file: "
					+ file.toAbsolutePath());
			}
		}
		if (targetDirectory == null)
			throw new IllegalArgumentException("targetDirectory may not be null");
		if (!Files.exists(targetDirectory))
		{
			throw new IllegalArgumentException("targetDirectory must exist: " + targetDirectory.
				toAbsolutePath());
		}
		if (!Files.isDirectory(targetDirectory))
		{
			throw new IllegalArgumentException("targetDirectory must be a directory: " + targetDirectory.
				toAbsolutePath());
		}
		Set<Path> uniqueSourceFiles = ImmutableSet.copyOf(sourceFiles);
		Set<Path> uniqueSourcePath = ImmutableSet.copyOf(sourcePath);
		final javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
		{
			throw new AssertionError("javax.tools.JavaCompiler is not available. Is tools.jar missing "
				+ "from the classpath?");
		}
		final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null,
			null);

		Iterable<? extends JavaFileObject> compilationUnits;
		try
		{
			Set<File> modifiedFiles = getModifiedFiles(uniqueSourceFiles, uniqueSourcePath,
				targetDirectory, new HashSet<Path>());
			if (modifiedFiles.isEmpty())
				return;
			compilationUnits = fileManager.getJavaFileObjectsFromFiles(modifiedFiles);
		}
		catch (IOException e)
		{
			throw new CompilationException(e);
		}
		final List<Path> effectiveClasspath = new ArrayList<>();
		effectiveClasspath.add(targetDirectory);
		effectiveClasspath.addAll(classPath);
		final List<String> options = new ArrayList<>();
		options.add("-cp");
		options.add(Joiner.on(File.pathSeparatorChar).join(effectiveClasspath));

		final StringBuilder debugLine = new StringBuilder("-g:");
		for (DebugType type : debugOptions)
		{
			switch (type)
			{
				case LINES:
				{
					debugLine.append("lines,");
					break;
				}
				case SOURCE:
				{
					debugLine.append("source,");
					break;
				}
				case VARIABLES:
				{
					debugLine.append("vars,");
					break;
				}
				default:
					throw new AssertionError(type);
			}
		}
		if (!debugOptions.isEmpty())
		{
			debugLine.deleteCharAt(debugLine.length() - ",".length());
			options.add(debugLine.toString());
		}

		if (!uniqueSourcePath.isEmpty())
		{
			options.add("-sourcepath");
			options.add(Joiner.on(File.pathSeparatorChar).join(uniqueSourcePath));
		}
		options.add("-s");
		options.add(targetDirectory.toString());
		options.add("-d");
		options.add(targetDirectory.toString());
		final Writer output = null;
		final CompilationTask task = compiler.getTask(output, fileManager, diagnostics, options, null,
			compilationUnits);
		final boolean result = task.call();
		try
		{
			printDiagnostics(diagnostics, options, sourceFiles);
		}
		catch (IOException e)
		{
			throw new BuildException(e);
		}
		if (!result)
			throw new CompilationException();
		try
		{
			fileManager.close();
		}
		catch (IOException e)
		{
			throw new BuildException(e);
		}
	}

	/**
	 * Returns the java source-code file corresponding to a class name.
	 * <p/>
	 * @param className the fully-qualified class name to look up
	 * @param sourcePath the source-code search path
	 * @return null if no match was found
	 */
	private static File getJavaSource(String className, Set<File> sourcePath)
	{
		// TODO: check for class files instead of source
		for (File path : sourcePath)
		{
			File result = classNameToFile(path, className);
			if (!result.exists())
				continue;
			return result;
		}
		return null;
	}

	/**
	 * Converts a class name to its source-code file.
	 * <p/>
	 * @param sourcePath the source-code search path
	 * @param className the fully-qualified class name
	 * @return the source-code file
	 */
	private static File classNameToFile(File sourcePath, String className)
	{
		return new File(sourcePath, className.replace(".", "/") + ".java");
	}

	/**
	 * Displays any compilation errors.
	 * <p/>
	 * @param diagnostics the compiler diagnostics
	 * @param options the command-line options passed to the compiler
	 * @param sourceFiles the source files to compile
	 * @throws IOException if an I/O error occurs
	 */
	private void printDiagnostics(final DiagnosticCollector<JavaFileObject> diagnostics,
		final List<String> options, final Collection<Path> sourceFiles) throws IOException
	{
		Logger log = LoggerFactory.getLogger(JavaCompiler.class.getName() + ".stderr");
		int errors = 0;
		int warnings = 0;
		boolean firstTime = true;
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
		{
			if (firstTime)
			{
				firstTime = false;
				StringBuilder message = new StringBuilder();
				message.append("Invoking: javac ");
				for (String token : options)
					message.append(token).append(" ");
				message.append(Joiner.on(" ").join(sourceFiles));
				log.debug(message.toString());
			}
			JavaFileObject source = diagnostic.getSource();
			if (source == null)
				log.error(diagnostic.getMessage(null));
			else
			{
				StringBuilder message = new StringBuilder();
				message.append(source.getName());
				if (diagnostic.getLineNumber() != Diagnostic.NOPOS)
					message.append(":").append(diagnostic.getLineNumber());
				message.append(": ").append(diagnostic.getMessage(null));
				log.error(message.toString());
				try (BufferedReader reader =
					new BufferedReader(new InputStreamReader(source.openInputStream())))
				{
					String line = null;
					for (long lineNumber = 0, size = diagnostic.getLineNumber(); lineNumber < size;
						++lineNumber)
					{
						line = reader.readLine();
						if (line == null)
							break;
					}
					if (line != null)
					{
						log.error(line);
						message = new StringBuilder();
						for (long i = 1, size = diagnostic.getColumnNumber(); i < size; ++i)
							message.append(" ");
						message.append("^");
						log.error(message.toString());
					}
				}
			}
			switch (diagnostic.getKind())
			{
				case ERROR:
				{
					++errors;
					break;
				}
				case NOTE:
				case OTHER:
				case WARNING:
				case MANDATORY_WARNING:
				{
					++warnings;
					break;
				}
				default:
					throw new AssertionError(diagnostic.getKind());
			}
		}
		if (errors > 0)
		{
			StringBuilder message = new StringBuilder();
			message.append(errors).append(" error");
			if (errors > 1)
				message.append("s");
			log.error(message.toString());
		}
		if (warnings > 0)
		{
			StringBuilder message = new StringBuilder();
			message.append(warnings).append(" warning");
			if (warnings > 1)
				message.append("s");
			log.warn(message.toString());
		}
	}

	/**
	 * Returns the source-code files that have been modified.
	 * <p/>
	 * @param sourceFiles the source files to process
	 * @param sourcePath the source file search path
	 * @param targetDirectory the directory to compile into
	 * @param unmodifiedFiles files that are known not to have been modified
	 * @return all changed source-code files that are accepted by the filter
	 * @throws IOException if an I/O error occurs
	 */
	private Set<File> getModifiedFiles(final Set<Path> sourceFiles,
		final Set<Path> sourcePath, final Path targetDirectory, final Set<Path> unmodifiedFiles)
		throws IOException
	{
		// Ant "depend" checks most dependencies for changes, but misses some cases.
		// Maven does not check dependencies for changes at all.
		//
		// @see http://stackoverflow.com/questions/7945705/does-maven-compiler-plugin-consider-dependencies-when-checking-for-stale-sources
		// @see http://ant.apache.org/manual/Tasks/depend.html

		// TODO: implement
		Set<File> result = new HashSet<>();
		for (Path path : sourceFiles)
			result.add(path.toFile());
		return result;
//		// TODO: Recompile modified dependencies even if user hasn't asked us to
//		Set<File> result = Sets.newHashSet(sourceFiles);
//		for (File sourceFile: sourceFiles)
//		{
//			if (unmodifiedFiles.contains(sourceFile))
//			{
//				result.remove(sourceFile);
//				continue;
//			}
//			FileNode source = dependencyCache.files.get(sourceFile);
//
//			if (source == null)
//				continue;
//
//			if (sourceFile.lastModified() > source.lastCompiled)
//				continue;
//			boolean targetOutOfDate = false;
//			for (String classInFile: source.classes)
//			{
//				if (sourceFile.lastModified() > JavaSource.getClassFile(classInFile, targetDirectory).
//					lastModified())
//				{
//					targetOutOfDate = true;
//					break;
//				}
//			}
//			if (targetOutOfDate)
//				continue;
//			boolean dependencyModified = false;
//			for (String className: source.classes)
//			{
//				for (String dependency: dependencyCache.classes.get(className).dependencies)
//				{
//					File dependencyFile = getJavaSource(dependency, sourcePath);
//					if (dependencyFile == null)
//					{
//						log.debug("Cannot find class: " + dependency);
//						continue;
//					}
//					if (!getModifiedFiles(Collections.singleton(dependencyFile), sourcePath, targetDirectory,
//						unmodifiedFiles).isEmpty())
//					{
//						dependencyModified = true;
//						break;
//					}
//				}
//				if (!dependencyModified)
//					break;
//			}
//			if (!dependencyModified)
//			{
//				unmodifiedFiles.add(sourceFile);
//				result.remove(sourceFile);
//			}
//		}
//		return result;
	}

	/**
	 * Returns the command-line representation of the object.
	 * <p/>
	 * @param sourceFiles the source files to compile
	 * @param targetDirectory the directory to compile into
	 * @param sourcePath the source file search path
	 * @return the command-line representation of the object
	 * @throws IllegalArgumentException if the classpath contains non-file components
	 * @throws IOException if an I/O error occurs
	 */
	private List<String> toCommandLine(final Collection<Path> sourceFiles, final Path targetDirectory,
		final Collection<Path> sourcePath)
		throws IOException
	{
		final List<String> result = Lists.newArrayList("javac");
		if (!classPath.isEmpty())
		{
			result.add("-cp");
			try
			{
				final StringBuilder line = new StringBuilder();
				for (final Iterator<Path> i = classPath.iterator(); i.hasNext();)
				{
					line.append(i.next().getParent().toString());
					if (i.hasNext())
						line.append(File.pathSeparatorChar);
				}
				result.add(line.toString());
			}
			catch (IllegalArgumentException e)
			{
				// Occurs if URL does not refer to a file
				throw new IllegalStateException(e);
			}
		}
		for (File javaFile : getModifiedFiles(ImmutableSet.copyOf(sourceFiles),
			ImmutableSet.copyOf(sourcePath), targetDirectory, new HashSet<Path>()))
		{
			result.add(javaFile.getPath());
		}
		result.add("-d");
		result.add(targetDirectory.getParent().toString());
		return result;
	}

	@Override
	public String toString()
	{
		return getClass().getName() + "[classPath=" + classPath + "]";
	}

	/**
	 * The type of debugging information that generated files may contain.
	 * <p/>
	 * @author Gili Tzabari
	 */
	@SuppressWarnings("PublicInnerClass")
	public enum DebugType
	{
		/**
		 * No debugging information.
		 */
		NONE,
		/**
		 * Line number information.
		 */
		LINES,
		/**
		 * Local variable information.
		 */
		VARIABLES,
		/**
		 * Source file information.
		 */
		SOURCE
	}
}
