package org.pill;

import com.google.common.base.Preconditions;

/**
 * A class that depends on the Guava module.
 * <p/>
 * @author Gili Tzabari
 */
public class DependsOnGuava
{
	public static void main(String[] args)
	{
		// External (inter-module) dependency
		Preconditions.checkArgument(args.length == 0, "This application does not accept arguments");
	}
}
