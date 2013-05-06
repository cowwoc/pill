package org.pill;

import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

/**
 * Helper functions for java.nio.Buffer.
 * <p/>
 * @author Gili Tzabari
 */
public final class Buffers
{
	/**
	 * Returns a ByteBuffer whose contents is identical but separate from the original buffer.
	 * Modifying the returned buffer will not affect the contents of the original buffer.
	 * <p/>
	 * @param original the buffer to copy
	 * @return an independent copy of original
	 * @throws NullPointerException if original is null
	 */
	public static ByteBuffer clone(ByteBuffer original)
	{
		Preconditions.checkNotNull(original, "original may not be null");

		ByteBuffer result = ByteBuffer.allocate(original.capacity());
		ByteBuffer source = original.duplicate();
		source.rewind();
		result.put(source);
		try
		{
			source.reset();
			result.position(source.position());
			result.mark();
		}
		catch (InvalidMarkException unused)
		{
			// Mark is unset, ignore.
		}

		result.position(original.position());
		result.limit(original.limit());
		return result;
	}

	/**
	 * Returns an array representation of a buffer. The returned buffer may, or may not, be tied to
	 * the underlying buffer's contents (so it should not be modified). The buffer's state is not
	 * modified by this method.
	 * <p/>
	 * @param buffer the buffer
	 * @return the remaining bytes
	 */
	public static byte[] toArray(ByteBuffer buffer)
	{
		if (buffer.hasArray() && !buffer.isReadOnly() && buffer.position() == 0
			&& buffer.remaining() == buffer.limit())
		{
			return buffer.array();
		}

		ByteBuffer copy = buffer.duplicate();
		byte[] result = new byte[copy.remaining()];
		copy.get(result);
		return result;
	}
}
