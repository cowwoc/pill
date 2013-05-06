package org.pill;

import java.nio.file.CopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Options for improving the performance of copy/move operations.
 * <p/>
 * @author Gili Tzabari
 */
public enum PerformanceCopyOption implements CopyOption
{
	/**
	 * Skip files with newer {@link BasicFileAttributes#lastModifiedTime() last-modified-time} than
	 * the source file.
	 */
	SKIP_NEWER
}
