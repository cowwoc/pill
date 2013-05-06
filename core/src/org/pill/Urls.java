package org.pill;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URL helper functions.
 * <p/>
 * @author Gili Tzabari
 */
public class Urls
{
	/**
	 * Downloads the contents of a URL to a file.
	 * <p/>
	 * @param source the URL to read from
	 * @param target the path to write to
	 * @throws IOException if an I/O error occurs
	 */
	@SuppressWarnings("SleepWhileInLoop")
	public static void download(URL source, Path target) throws IOException
	{
		Logger log = LoggerFactory.getLogger(Urls.class);
		log.debug("Downloading " + source + " to " + target);
		URLConnection connection = source.openConnection();
		try (ReadableByteChannel in = Channels.newChannel(connection.getInputStream()))
		{
			try (SeekableByteChannel out = Files.newByteChannel(target))
			{
				ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
				while (true)
				{
					int count = in.read(buffer);
					buffer.flip();
					if (count == -1 && buffer.remaining() == 0)
						break;
					if (count == 0)
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
							throw new IOException(e);
						}
					}
					out.write(buffer);
					buffer.compact();
				}
			}
		}
	}

	/**
	 * Prevent construction.
	 */
	private Urls()
	{
	}
}
