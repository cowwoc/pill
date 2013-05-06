package org.pill;

/**
 * Dependency types.
 * <p/>
 * @author Gili Tzabari
 */
public enum DependencyType
{
	/**
	 * A dependency needed to build a release.
	 */
	BUILD,
	/**
	 * A dependency needed to run a release.
	 */
	RUNTIME
}
