package org.pill;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Module helper functions.
 * <p/>
 * @author Gili Tzabari
 */
public final class Modules
{
	/**
	 * Returns the directory containing a class' default package. If the class is wrapped in an
	 * archive (i.e. JAR), returns the directory containing the archive.
	 * <p/>
	 * @param clazz the class
	 * @return the directory containing the application
	 * @throws IOException if an I/O error occurs while looking up the path
	 * @throws IllegalArgumentException if clazz was loaded from a network location
	 */
	public static Path getRootPath(Class<?> clazz) throws IOException
	{
		URL classUrl = clazz.getClassLoader().getResource(clazz.getName().replace('.', '/')
			+ ".class");
		// REFERENCE: http://stackoverflow.com/questions/8014099/how-do-i-convert-a-jarfile-uri-to-the-path-of-jar-file/8014184#8014184
		URLConnection connection = classUrl.openConnection();
		Path result;
		try
		{
			if (connection instanceof JarURLConnection)
			{
				// WORKAROUND: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7108548
				JarURLConnection jarConnection = (JarURLConnection) connection;
				result = Paths.get(jarConnection.getJarFileURL().toURI()).getParent();
			}
			else
			{
				result = Paths.get(classUrl.toURI());
				String packageName = clazz.getPackage().getName();
				int packageSize;
				if (packageName.isEmpty())
					packageSize = 0;
				else
					packageSize = packageName.split("\\.").length + 1;
				for (int i = 0; i < packageSize; ++i)
					result = result.getParent();
			}
		}
		catch (URISyntaxException e)
		{
			throw new AssertionError(e);
		}
		catch (FileSystemNotFoundException e)
		{
			throw new IllegalArgumentException("Clazz was loaded from a network location", e);
		}
		return result.toAbsolutePath();
	}
}
