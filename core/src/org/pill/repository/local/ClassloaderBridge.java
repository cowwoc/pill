package org.pill.repository.local;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.lang.reflect.Method;
import org.pill.repository.Repository;

/**
 * Holds references classes loaded by the Pill classloader for use by classes loaded by the JDK
 * (e.g. ReleaseFileSystemProvider).
 * <p/>
 * @author Gili Tzabari
 */
public class ClassloaderBridge
{
	private static ClassloaderBridge instance;
	private final Repository localRepository;

	/**
	 * Creates a new ClassloaderBridge.
	 * <p/>
	 * @param localRepository an instance of {@link LocalRepository}
	 */
	private ClassloaderBridge(Repository localRepository)
	{
		this.localRepository = localRepository;
	}

	/**
	 * @return the ClassloaderBridge
	 */
	public static ClassloaderBridge getInstance()
	{
		return instance;
	}

	/**
	 * Creates the ClassloaderBridge.
	 * <p/>
	 * @param classLoader the ClassLoader used to load all the classes being bridged
	 * @throws NullPointerException if classLoader is null
	 * @throws IOException if an error occurs while creating the instance
	 */
	public static void create(ClassLoader classLoader) throws IOException
	{
		Preconditions.checkNotNull(classLoader, "classLoader may not be null");
		if (instance != null)
			throw new IllegalStateException("Singleton instance already created");
		try
		{
			Class<?> localRepositoryClass = classLoader.loadClass(LocalRepository.class.getName());
			Method getInjector = localRepositoryClass.getMethod("getInstance");
			Repository localRepository = (Repository) getInjector.invoke(null);
			instance = new ClassloaderBridge(localRepository);
		}
		catch (ReflectiveOperationException e)
		{
			throw new IOException("Exception while running PillInjector.getInstance()", e);
		}
	}

	/**
	 * @return an instance of {@link LocalRepository}
	 */
	public Repository getLocalRepository()
	{
		return localRepository;
	}
}
