package org.pill;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A ClassLoader that allows its classpath to be appended.
 * <p/>
 * @author Gili Tzabari
 */
public class AppendableClassLoader extends URLClassLoader
{
	/**
	 * Creates a new AppendableClassLoader.
	 * <p/>
	 * @param parent the parent classpath
	 */
	public AppendableClassLoader(ClassLoader parent)
	{
		super(new URL[0], parent);
	}
	
	/**
	 * Creates a new AppendableClassLoader that delegates to the system classloader.
	 */
	public AppendableClassLoader()
	{
		this(null);
	}

	@Override
	protected void addURL(URL url)
	{
		super.addURL(url);
	}
}
