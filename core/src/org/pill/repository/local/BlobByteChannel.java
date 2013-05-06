package org.pill.repository.local;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.mysema.query.QueryException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Set;
import org.pill.sql.Session;

/**
 * A SeekableByteChannel for reading/writing BLOBs. The BLOB is kept open during the lifetime of the
 * channel.
 * <p/>
 * <b>THREAD SAFETY</b>: This class is not thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
public class BlobByteChannel implements SeekableByteChannel
{
	private final Set<StandardOpenOption> options;
	private final Blob blob;
	private final Session session;
	private long position;
	private boolean needToAppend;
	private boolean closed;

	/**
	 * Creates a new BlobByteChannel.
	 * <p/>
	 * @param options the open options
	 * @param blob the blob to wrap
	 * @param session the database session associated with the blob
	 * @throws NullPointerException if options, blob or session are null
	 */
	public BlobByteChannel(Set<StandardOpenOption> options, Blob blob, Session session)
	{
		Preconditions.checkNotNull(options, "options may not be null");
		Preconditions.checkNotNull(blob, "blob may not be null");
		Preconditions.checkNotNull(session, "session may not be null");

		this.options = ImmutableSet.copyOf(options);
		this.needToAppend = options.contains(StandardOpenOption.APPEND);
		this.blob = blob;
		this.session = session;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException
	{
		if (closed)
			throw new ClosedChannelException();
		if (!options.contains(StandardOpenOption.READ))
			throw new NonReadableChannelException();
		byte[] data;
		long length;
		try
		{
			length = blob.length();
			if (position > length)
			{
				// Behavior mentioned in position(long) Javadoc
				return -1;
			}

			// BLOB positions are 1-based
			data = blob.getBytes(position + 1, dst.remaining());
			position += data.length;
		}
		catch (SQLException e)
		{
			throw new IOException(e);
		}
		dst.put(data);
		if (data.length == 0 && position == length)
			return -1;
		return data.length;
	}

	@Override
	public int write(ByteBuffer src) throws IOException
	{
		if (closed)
			throw new ClosedChannelException();
		if (!options.contains(StandardOpenOption.WRITE))
			throw new NonWritableChannelException();
		try
		{
			long length = blob.length();
			if (needToAppend)
			{
				needToAppend = false;
				position = length;
			}
			if (position > length)
			{
				byte[] buffer = new byte[0];
				do
				{
					// Grow BLOB by 1MB at a time
					int bufferSize = Math.min(1_000_000, Ints.saturatedCast(position - length));
					if (buffer.length != bufferSize)
						buffer = new byte[bufferSize];
					blob.setBytes(length, buffer);
				}
				while (position > length);
			}

			// Write to BLOB 1MB at a time
			int bufferSize = Math.min(1_000_000, src.remaining());
			byte[] buffer = new byte[bufferSize];
			int result = 0;
			while (src.hasRemaining())
			{
				int len = src.remaining();
				src.get(buffer, 0, len);
				int offset = 0;
				while (len > 0)
				{
					// BLOB positions are 1-based
					int written = blob.setBytes(position + 1, buffer, offset, len);
					offset += written;
					len -= written;
				}
				result += offset;
			}
			position += result;
			return result;
		}
		catch (SQLException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public long position() throws IOException
	{
		if (closed)
			throw new ClosedChannelException();
		return position;
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException
	{
		Preconditions.checkArgument(newPosition >= 0, "newPosition may not be negative");
		if (closed)
			throw new ClosedChannelException();
		position = newPosition;
		return this;
	}

	@Override
	public long size() throws IOException
	{
		if (closed)
			throw new ClosedChannelException();
		try
		{
			return blob.length();
		}
		catch (SQLException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isOpen()
	{
		return !closed;
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			blob.free();
		}
		catch (SQLFeatureNotSupportedException unused)
		{
			// ignore
		}
		catch (SQLException e)
		{
			throw new IOException(e);
		}
		try
		{
			session.close();
		}
		catch (QueryException e)
		{
			throw new IOException(e);
		}
		closed = true;
	}
}
