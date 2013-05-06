package org.pill;

import java.util.Comparator;

/**
 * A comparator that uses the natural ordering of the objects being compared.
 * <p/>
 * @param <T> the type of objects being compared
 * @author Gili Tzabari
 */
public final class NaturalComparator<T extends Comparable<T>> implements Comparator<T>
{
	@Override
	public int compare(T left, T right)
	{
		if (right == null)
			throw new NullPointerException("right may not be null");
		if (left == right)
			return 0;
		return left.compareTo(right);
	}
}
