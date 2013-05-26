package org.pill;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point to the Pill library.
 * <p/>
 * <b>THREAD-SAFETY</b>: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
@Singleton
public class Pill
{
	private List<Path> classpath;
	private final Logger log = LoggerFactory.getLogger(Pill.class);

	/**
	 * Returns the classpath associated with the ClassLoader.
	 * <p/>
	 * @param classLoader a ClassLoader (null denotes the system ClassLoader)
	 * @return empty list if an unknown ClassLoader is encountered
	 * @throws URISyntaxException if an error occurs while converting a URL to a URI
	 */
	private List<Path> getClassPath(ClassLoader classLoader) throws URISyntaxException
	{
		log.debug("Entering. classLoader: " + classLoader);
		if (classLoader instanceof URLClassLoader)
		{
			URLClassLoader urlClassloader = (URLClassLoader) classLoader;
			List<Path> result = new ArrayList<>();
			for (URL url: urlClassloader.getURLs())
				result.add(Paths.get(url.toURI()));
			log.debug("Returning {}", result);
			return result;
		}
		if (classLoader == null || classLoader.equals(ClassLoader.getSystemClassLoader()))
		{
			List<Path> result = new ArrayList<>();
			for (String path: System.getProperty("java.class.path").
				split(System.getProperty("path.separator")))
			{
				result.add(Paths.get(path));
			}
			log.debug("Returning {}", result);
			return result;
		}
		log.warn("Unknown classloader type: " + classLoader.getClass().getName());
		List<Path> result = ImmutableList.of();
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * @return a path containing the Pill classes and their dependencies
	 * @throws IOException if an I/O error occurs while resolving the classpath
	 */
	public List<Path> getClassPath() throws IOException
	{
		log.debug("Entering");
		if (classpath == null)
		{
			List<Path> result = new ArrayList<>();
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			try
			{
				while (cl != null)
				{
					result.addAll(0, getClassPath(cl));
					cl = cl.getParent();
				}
			}
			catch (URISyntaxException e)
			{
				throw new IOException(e);
			}
			this.classpath = result;
		}
		log.debug("Returning {}", classpath);
		return classpath;
	}

	/**
	 * @param args the command line arguments
	 * @throws IOException if an I/O error occurs while building the project
	 * @throws CompilationException if an error occurs while compiling the project
	 */
	public static void main(String[] args) throws IOException, CompilationException
	{
		Path currentDirectory = Paths.get(System.getProperty("user.dir")).resolve("pill");
		Pill pill = new Pill();
		new ScriptBuilder(currentDirectory).classPath(pill.getClassPath()).run();
	}
}
