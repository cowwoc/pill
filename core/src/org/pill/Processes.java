package org.pill;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Process helper functions.
 * <p/>
 * @author Gili Tzabari
 */
public class Processes
{
	/**
	 * Launches and waits for a process to complete.
	 * <p/>
	 * @param processBuilder the process to launch
	 * @param out the stream to write the process output into
	 * @throws IOException if an I/O error occurs while running the process
	 * @throws InterruptedException if the thread was interrupted
	 */
	public static int waitFor(ProcessBuilder processBuilder, PrintStream out)
		throws IOException, InterruptedException
	{
		Process process = processBuilder.redirectErrorStream(true).start();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream())))
		{
			while (true)
			{
				String line = in.readLine();
				if (line == null)
					break;
				out.println(line);
			}
		}
		return process.waitFor();
	}
}
