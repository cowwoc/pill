package org.pill;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility functions for enumerations.
 * <p/>
 * @author Gili Tzabari
 */
public class Enumerations
{
	/**
	 * Joins enumerations.
	 * <p/>
	 * @param <T> the enumeration type
	 * @param enumerations the list of enumerations to join
	 * @return a single enumeration that iterates over the elements of the input enumerations
	 */
	public static <T> Enumeration<T> join(List<Enumeration<T>> enumerations)
	{
		return new EnumeratorJoiner<>(enumerations);
	}

	/**
	 * @param <T> the element type
	 * @return an empty Enumeration
	 */
	public static <T> Enumeration<T> empty()
	{
		return new Enumeration<T>()
		{
			@Override
			public boolean hasMoreElements()
			{
				return false;
			}

			@Override
			public T nextElement()
			{
				throw new NoSuchElementException();
			}
		};
	}

	/**
	 * Joins enumerations.
	 */
	private static class EnumeratorJoiner<T> implements Enumeration<T>
	{
		private final Iterator<Enumeration<T>> iterator;
		private Enumeration<T> current;

		public EnumeratorJoiner(List<Enumeration<T>> enumerations)
		{
			this.iterator = enumerations.iterator();
			if (iterator.hasNext())
				this.current = iterator.next();
			else
				this.current = null;
		}

		@Override
		public boolean hasMoreElements()
		{
			while (iterator.hasNext())
			{
				while (current.hasMoreElements())
					return true;
				current = iterator.next();
			}
			return false;
		}

		@Override
		public T nextElement()
		{
			if (!hasMoreElements())
				throw new NoSuchElementException();
			return current.nextElement();
		}
	}
}
