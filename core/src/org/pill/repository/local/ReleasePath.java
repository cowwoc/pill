package org.pill.repository.local;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The path of a file belonging to a Release.
 * <p/>
 * <b>URI syntax</b>: {@code org.pill.repository.local.release:<id>:<path>}
 * <p/>
 * Where {@code <id>} is the database id of the Release and {@code <path>} is the path of file.
 * <p/>
 * @author Gili Tzabari
 */
public final class ReleasePath implements Path
{
	private final ReleaseFileSystem filesystem;
	private final String path;
	/**
	 * The index of name components, initialized lazily.
	 * <p/>
	 * The array contains one element more than the number of name components. This final element
	 * contains the index that a child name component would be inserted at.
	 */
	private int[] components;
	private final int SEPARATOR_LENGTH = 1;

	/**
	 * Creates a new ReleasePath.
	 * <p/>
	 * @param filesystem the filesystem associated with the path
	 * @param path the file path
	 * @throws NullPointerException if filesystem or path are null
	 */
	public ReleasePath(ReleaseFileSystem filesystem, String path)
	{
		Preconditions.checkNotNull(filesystem, "filesystem may not be null");
		Preconditions.checkNotNull(path, "path may not be null");

		this.filesystem = filesystem;
		this.path = path.trim();
	}

	/**
	 * Initializes the index of name components, if necessary.
	 */
	private void initComponents()
	{
		if (components == null)
		{
			String[] tokens = path.split("/");
			int tokenOffset;
			if (path.startsWith("/"))
			{
				// Skip the first (empty) token
				tokenOffset = 1;
			}
			else
				tokenOffset = 0;
			components = new int[tokens.length + 1 - tokenOffset];
			if (components.length > 0)
				components[0] = tokenOffset;
			for (int i = 0; i < tokens.length; ++i)
				components[i + 1] = components[i] + tokens[tokenOffset + i].length() + SEPARATOR_LENGTH;
		}
	}

	@Override
	public ReleaseFileSystem getFileSystem()
	{
		return filesystem;
	}

	@Override
	public boolean isAbsolute()
	{
		return !path.isEmpty() && path.charAt(0) == '/';
	}

	/**
	 * @return true if the path is a root
	 */
	public boolean isRoot()
	{
		return path.equals("/");
	}

	@Override
	public ReleasePath getRoot()
	{
		// DESIGN: We are using unix-style paths.
		//
		// For Unix paths, '/' indicates an absolute path and is a path root.
		// For Windows paths, '\\' indicates an absolute path but "C:" is a path root.

		if (!isAbsolute())
			return null;
		if (path.length() == 1)
			return this;
		return new ReleasePath(filesystem, "/");
	}

	/**
	 * Returns a name component as a String.
	 * <p/>
	 * @param index the index of the element
	 * @return the name component
	 * @throws ArrayIndexOutOfBoundsException if {@code index >= getNameCount()}
	 */
	private String getNameString(int index)
	{
		return path.substring(components[index], components[index + 1] - SEPARATOR_LENGTH);
	}

	@Override
	public ReleasePath getFileName()
	{
		initComponents();
		if (components.length == 1)
			return null;
		if (components.length == 2 && components[0] == 0)
			return this;
		return new ReleasePath(filesystem, getNameString(getNameCount() - 1));
	}

	@Override
	public ReleasePath getParent()
	{
		initComponents();
		if (components.length == 1)
			return null;
		return new ReleasePath(filesystem,
			path.substring(0, components[getNameCount() - 1] - SEPARATOR_LENGTH));
	}

	@Override
	public int getNameCount()
	{
		initComponents();
		return components.length - 1;
	}

	@Override
	public ReleasePath getName(int index)
	{
		Preconditions.checkArgument(index >= 0, "index may not be negative");
		initComponents();
		Preconditions.checkArgument(index < getNameCount(), "index may not be greater than "
			+ getNameCount());
		return new ReleasePath(filesystem, getNameString(index));
	}

	@Override
	public ReleasePath subpath(int beginIndex, int endIndex)
	{
		initComponents();
		final int nameCount = getNameCount();
		Preconditions.checkArgument(beginIndex >= 0 && beginIndex < nameCount,
			"beginIndex must be [0, " + (nameCount - 1) + "]. Was: " + beginIndex);
		Preconditions.checkArgument(endIndex > beginIndex && beginIndex <= nameCount,
			"endIndex must be [" + (beginIndex + 1) + ", " + nameCount + "]. Was: " + beginIndex);
		if (beginIndex == 0 && endIndex == nameCount)
			return this;

		return new ReleasePath(filesystem,
			path.substring(components[beginIndex], components[endIndex] - SEPARATOR_LENGTH));
	}

	@Override
	public boolean startsWith(Path o)
	{
		Preconditions.checkNotNull(o, "other may not be null");
		if (!(o instanceof ReleasePath))
			return false;
		if (!o.getFileSystem().equals(filesystem))
			return false;
		ReleasePath other = (ReleasePath) o;

		initComponents();
		List<Integer> thisTokens = Ints.asList(components);

		other.initComponents();
		@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
		List<Integer> otherTokens = Ints.asList(other.components);

		return Collections.indexOfSubList(thisTokens, otherTokens) == 0;
	}

	@Override
	public boolean startsWith(String other)
	{
		return startsWith(new ReleasePath(filesystem, other));
	}

	@Override
	public boolean endsWith(Path o)
	{
		Preconditions.checkNotNull(o, "other may not be null");
		if (!(o instanceof ReleasePath))
			return false;
		if (!o.getFileSystem().equals(filesystem))
			return false;
		ReleasePath other = (ReleasePath) o;

		initComponents();
		List<Integer> thisTokens = Ints.asList(components);

		other.initComponents();
		@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
		List<Integer> otherTokens = Ints.asList(other.components);

		int index = Collections.lastIndexOfSubList(thisTokens, otherTokens);
		return index != -1 && index == (thisTokens.size() - otherTokens.size());
	}

	@Override
	public boolean endsWith(String other)
	{
		return endsWith(new ReleasePath(filesystem, other));
	}

	@Override
	public ReleasePath normalize()
	{
		// Process components from right to left
		initComponents();

		List<String> normalized = new ArrayList<>();
		int skip = 0;
		for (int i = components.length - 2; i >= 0; --i)
		{
			if (skip > 0)
			{
				--skip;
				continue;
			}
			String component = getNameString(i);
			switch (component)
			{
				case ".":
					continue;
				case "..":
				{
					++skip;
					continue;
				}
			}
			normalized.add(component);
		}
		return new ReleasePath(filesystem, Joiner.on('/').join(Lists.reverse(normalized)));
	}

	@Override
	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
	public ReleasePath resolve(Path o)
	{
		Preconditions.checkNotNull(o, "other may not be null");
		if (!(o instanceof ReleasePath))
		{
			throw new ProviderMismatchException("other's provider was " + o.getFileSystem().provider().
				getClass().getName() + " instead of " + filesystem.provider().getClass().getName());
		}
		ReleasePath other = (ReleasePath) o;
		if (other.isAbsolute())
			return other;
		if (other.path.isEmpty())
			return this;
		return new ReleasePath(filesystem, path + '/' + other.path);
	}

	@Override
	public ReleasePath resolve(String other)
	{
		return resolve(new ReleasePath(filesystem, other));
	}

	@Override
	public ReleasePath resolveSibling(Path o)
	{
		Preconditions.checkNotNull(o, "other may not be null");
		if (!(o instanceof ReleasePath))
		{
			throw new ProviderMismatchException("other's provider was " + o.getFileSystem().provider().
				getClass().getName() + " instead of " + filesystem.provider().getClass().getName());
		}
		ReleasePath other = (ReleasePath) o;
		if (path.isEmpty())
			return other;
		return getParent().resolve(o);
	}

	@Override
	public ReleasePath resolveSibling(String other)
	{
		return resolveSibling(new ReleasePath(filesystem, other));
	}

	@Override
	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
	public ReleasePath relativize(Path o)
	{
		Preconditions.checkNotNull(o, "other may not be null");

		if (this.equals(o))
			return new ReleasePath(filesystem, "");
		if (!(o instanceof ReleasePath))
		{
			throw new ProviderMismatchException("other's provider was " + o.getFileSystem().provider().
				getClass().getName() + " instead of " + filesystem.provider().getClass().getName());
		}
		ReleasePath other = (ReleasePath) o;
		String thisRoot = getRoot().toString();
		String otherRoot = other.getRoot().toString();
		Preconditions.checkArgument(thisRoot != null && otherRoot == null,
			"this path has a root component but other does not");
		Preconditions.checkArgument(thisRoot == null && otherRoot != null,
			"this path does not have a root component but other does");

		ReleasePath thisNormalized = normalize();
		ReleasePath otherNormalized = other.normalize();

		thisNormalized.initComponents();
		otherNormalized.initComponents();
		StringBuilder result = new StringBuilder();

		// Skip common directories
		int i = 0;
		for (int size = Math.min(thisNormalized.components.length, otherNormalized.components.length);
			i < size; ++i)
		{
			if (thisNormalized.components[i] != otherNormalized.components[i])
				break;
		}

		// Step out of remaining directories in the base path
		for (int j = i; j < thisNormalized.components.length; ++j)
			result.append("../");

		// Append the remaining directories in the other path
		for (int j = i; j < otherNormalized.components.length; ++j)
			result.append(otherNormalized.getName(i)).append('/');

		// remove final separator
		result.deleteCharAt(result.length() - SEPARATOR_LENGTH);
		return new ReleasePath(filesystem, result.toString());
	}

	@Override
	public URI toUri()
	{
		try
		{
			return new URI(filesystem.getRelease() + ":" + path);
		}
		catch (URISyntaxException e)
		{
			throw new IOError(e);
		}
	}

	@Override
	public ReleasePath toAbsolutePath()
	{
		if (isAbsolute())
			return this;
		ReleasePath normalized = normalize();
		return normalized; //filesystem.getCurrentPath().resolve(normalized);
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException
	{
		Preconditions.checkNotNull(options, "options may not be null");

		return toAbsolutePath();
	}

	@Override
	public File toFile()
	{
		throw new UnsupportedOperationException("Path is not associated with the default provider");
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers)
		throws IOException
	{
		throw new UnsupportedOperationException("ModulePath does not support WatchService");
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException
	{
		throw new UnsupportedOperationException("ModulePath does not support WatchService");
	}

	@Override
	public Iterator<Path> iterator()
	{
		// URI syntax: org.pill.repository.local.release:<id>:<path>
		return new Iterator<Path>()
		{
			private int index;

			@Override
			public boolean hasNext()
			{
				return index < getNameCount() - 1;
			}

			@Override
			public Path next()
			{
				Path result = getName(index);
				++index;
				return result;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException("Path is immutable");
			}
		};
	}

	@Override
	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
	public int compareTo(Path o)
	{
		ReleasePath other = (ReleasePath) o;
		return path.compareTo(other.path);
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof ReleasePath))
			return false;
		ReleasePath other = (ReleasePath) o;
		if (!filesystem.equals(other.getFileSystem()))
			return false;
		return path.equals(other.toString());
	}

	@Override
	public int hashCode()
	{
		return path.hashCode();
	}

	@Override
	public String toString()
	{
		return path;
	}
}
