package org.pill;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generates a JSON-style representation of an Object. The output is meant to facilitate
 * readability, nothing more. As such, it does not adhere perfectly to the JSON standard.
 * <p/>
 * @author Gili Tzabari
 */
public final class ToJsonString
{
	/**
	 * Styles for opening braces.
	 */
	@SuppressWarnings("PublicInnerClass")
	public static enum BraceStyle
	{
		/**
		 * Opening brace should go on the same line as the key.
		 */
		SAME_LINE,
		/**
		 * Opening brace should go on the line after the key.
		 */
		NEW_LINE;
	}
	private final StringBuilder builder;
	private BraceStyle braceStyle = BraceStyle.NEW_LINE;
	private int indentSize = 2;
	private final Map<String, Object> map = Maps.newHashMap();

	/**
	 * Creates a new ToJsonString.
	 * <p/>
	 * @param className the name of the class we are processing
	 * @throws NullPointerException if className or braceStyle are null
	 * @throws IllegalArgumentException if indentSize is negative
	 */
	public ToJsonString(String className)
	{
		Preconditions.checkNotNull(className, "className may not be null");

		this.builder = new StringBuilder(32).append(className);
	}

	/**
	 * Creates a new ToJsonString. Uses Reflection to associate the names of all fields with their
	 * values. This method does not process superclass properties.
	 * <p/>
	 * @param clazz the class whose fields to process
	 * @param obj the object whose values to read
	 * @throws IllegalArgumentException if the specified object is not an instance of the class or
	 * interface declaring the underlying field (or a subclass or implementor thereof)
	 */
	public ToJsonString(Class<?> clazz, Object obj)
	{
		this(clazz.getName());
		putAllFields(clazz, obj);
	}

	/**
	 * Sets the number of characters to use when indenting output.
	 * <p/>
	 * @param indentSize the number of characters to use when indenting output
	 * @throws IllegalArgumentException if indentSize is negative
	 * @return this
	 */
	public ToJsonString setIndentSize(int indentSize)
	{
		Preconditions.checkArgument(indentSize >= 0, "indentSize must be non-negative");

		this.indentSize = indentSize;
		return this;
	}

	/**
	 * Sets the style to use for opening braces.
	 * <p/>
	 * @param braceStyle the style to use for opening braces
	 * @return this
	 * @throws NullPointerException if braceStyle is null
	 */
	public ToJsonString setBraceStyle(BraceStyle braceStyle)
	{
		Preconditions.checkNotNull(braceStyle, "braceStyle may not be null");

		this.braceStyle = braceStyle;
		return this;
	}

	/**
	 * Uses Reflection to associate the names of all fields with their values. This method does not
	 * process superclass properties.
	 * <p/>
	 * @param clazz the class whose fields to process
	 * @param obj the object whose values to read
	 * @return this
	 * @throws NullPointerException if clazz or obj are null
	 * @throws IllegalArgumentException if the specified object is not an instance of the class or
	 * interface declaring the underlying field (or a subclass or implementor thereof)
	 */
	public ToJsonString putAllFields(final Class<?> clazz, final Object obj)
	{
		Preconditions.checkNotNull(clazz, "clazz may not be null");
		Preconditions.checkNotNull(obj, "obj may not be null");

		if (!clazz.isAssignableFrom(obj.getClass()))
			throw new IllegalArgumentException("obj must be an instance of " + clazz);
		AccessController.doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				try
				{
					for (Field field : clazz.getDeclaredFields())
					{
						field.setAccessible(true);
						put(field.getName(), field.get(obj));
					}
				}
				catch (IllegalAccessException e)
				{
					throw new AssertionError(e);
				}
				return null;
			}
		});
		return this;
	}

	/**
	 * Associates the specified value with the specified key. If the key was already associated with a
	 * value, the old value is replaced by the specified value. If {@code value} is {@code null}, the
	 * string {@code "null"} is used.
	 * <p/>
	 * @param key the key
	 * @param value the value
	 * @return this
	 */
	public ToJsonString put(String key, Object value)
	{
		Preconditions.checkNotNull(key, "key may not be null");

		map.put(key, value);
		return this;
	}

	@Override
	public String toString()
	{
		switch (braceStyle)
		{
			case NEW_LINE:
			{
				this.builder.append("\n{\n");
				break;
			}
			case SAME_LINE:
			{
				this.builder.append(" {\n");
				break;
			}
			default:
				throw new AssertionError("Unexpected braceStyle: " + braceStyle);
		}
		String separator = "";
		String indent = Strings.repeat(" ", indentSize);
		for (Entry<String, Object> entry : map.entrySet())
		{
			builder.append(separator).append(indent).append("\"").append(entry.getKey()).append("\": ");
			Object value = entry.getValue();
			if (value instanceof String)
				builder.append("\"");
			if (value == null)
				builder.append("null");
			else if (value.getClass().isArray())
			{
				String arrayValue;
				if (value instanceof Object[])
					arrayValue = Arrays.toString((Object[]) value);
				else if (value instanceof byte[])
					arrayValue = Arrays.toString((byte[]) value);
				else if (value instanceof short[])
					arrayValue = Arrays.toString((short[]) value);
				else if (value instanceof int[])
					arrayValue = Arrays.toString((int[]) value);
				else if (value instanceof long[])
					arrayValue = Arrays.toString((long[]) value);
				else if (value instanceof char[])
					arrayValue = Arrays.toString((char[]) value);
				else if (value instanceof float[])
					arrayValue = Arrays.toString((float[]) value);
				else if (value instanceof double[])
					arrayValue = Arrays.toString((double[]) value);
				else if (value instanceof boolean[])
					arrayValue = Arrays.toString((boolean[]) value);
				else
					throw new AssertionError("Unknown array type: " + value.getClass().getName());
				builder.append(arrayValue.replace("\n", "\n" + indent));
			}
			else
				builder.append(value.toString().replace("\n", "\n" + indent));
			if (value instanceof String)
				builder.append("\"");
			separator = ",\n";
		}
		return builder.append("\n}").toString();
	}
}
