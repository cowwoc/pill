package org.pill.repository.local;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.flyway.core.Flyway;
import com.mysema.query.sql.codegen.MetaDataExporter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.pill.Modules;

/**
 * Generates an empty database and exports query classes based on its schema.
 * <p/>
 * @author Gili Tzabari
 */
public class Build
{
	private final Injector injector = Guice.createInjector(new GuiceConfig());

	/**
	 * Migrates the database to the latest version.
	 */
	public void migrate()
	{
		Flyway flyway = new Flyway();
		DataSource dataSource = injector.getInstance(DataSource.class);
		flyway.setDataSource(dataSource);
		flyway.setLocations("org/pill/database/migration");
		flyway.migrate();
	}

	/**
	 * Export query classes.
	 * <p/>
	 * @throws SQLException if an error occurs while reading the database
	 * @throws IOException if an error occurs while locating the directory for the query classes
	 */
	public void exportQueries() throws SQLException, IOException
	{
		try (Connection connection = injector.getInstance(Connection.class))
		{
			MetaDataExporter exporter = new MetaDataExporter();
			exporter.setPackageName("org.pill.repository.local.queries");
			// Export classes into build directory
			exporter.setTargetFolder(Modules.getRootPath(Build.class).getParent().
				resolve("generated-sources/java").toFile());
			exporter.setSchemaPattern("PUBLIC");
			exporter.export(connection.getMetaData());
		}
	}

	/**
	 * Builds the database.
	 * <p/>
	 * @param args command-line arguments
	 * @throws SQLException if an error occurs while reading the database
	 * @throws IOException if an error occurs while locating the directory for the query classes
	 */
	public static void main(String[] args)
		throws IOException, SQLException
	{
		Build database = new Build();
		database.migrate();
		database.exportQueries();
	}
}
