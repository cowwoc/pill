package org.pill;

/**
 * Resolves class names to module dependencies.
 * <p/>
 * @author Gili Tzabari
 */
public interface DependencyResolver
{
	/**
	 * Returns the module that contains a specific class.
	 * <p/>
	 * @param className the {@link java.lang.Class#getName() class name}
	 * @return the module
	 * @throws ClassNotFoundException if the specified class could not be mapped to a module
	 */
	Module resolve(String className) throws ClassNotFoundException;
}
