package org.pill;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The entry point to the Pill library.
 * <p/>
 * <b>THREAD-SAFETY</b>: This class is thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
@Singleton
public class Pill
{
	/**
	 * @return a path containing the Pill classes and their dependencies
	 * @throws IOException if an I/O error occurs while resolving the classpath
	 */
	public List<Path> getClassPath() throws IOException
	{
		Path rootPath = Modules.getRootPath(Pill.class);
		return ImmutableList.of(rootPath,
			rootPath.resolve("../../lib/jsr305/jsr305-2.0.1.jar"),
			rootPath.resolve("../../lib/slf4j/slf4j-api-1.7.5.jar"),
			rootPath.resolve("../../lib/logback/logback-core-1.0.12.jar"),
			rootPath.resolve("../../lib/logback/logback-classic-1.0.12.jar"),
			rootPath.resolve("../../lib/querydsl-sql/querydsl-core-3.1.1.jar"),
			rootPath.resolve("../../lib/querydsl-sql/querydsl-sql-3.1.1.jar"),
			rootPath.resolve("../../lib/querydsl-sql/mysema-commons-lang-0.2.4.jar"),
			rootPath.resolve("../../lib/guava/guava-14.0.1.jar"),
			rootPath.resolve("../../lib/guice/aopalliance.jar"),
			rootPath.resolve("../../lib/guice/guice-3.0.jar"),
			rootPath.resolve("../../lib/guice/guice-servlet-3.0.jar"),
			rootPath.resolve("../../lib/h2/h2-1.3.171.jar"),
			rootPath.resolve("../../lib/flyway/flyway-core-2.1.1.jar"),
			rootPath.resolve("../../lib/JavaCompiler/tools.jar"),
			rootPath.resolve("../../lib/joda-time/joda-time-2.2.jar"));
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, CompilationException
	{
		Path currentDirectory = Paths.get(System.getProperty("user.dir")).resolve("pill");
		Pill pill = new Pill();
		new ScriptBuilder(currentDirectory).classPath(pill.getClassPath()).run();
	}
}
