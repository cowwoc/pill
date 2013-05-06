package org.pill.examples;

import com.google.common.base.Preconditions;

/**
 * @author Gili Tzabari
 */
public class HelloWorld
{
	public static void main(String[] args)
	{
		// External dependency on Guava module
		Preconditions.checkArgument(args.length == 0, "This application does not accept arguments");

		// Internal dependency on a another class in the same module
		System.out.println(new LocalDependency() + " World");
	}
}
