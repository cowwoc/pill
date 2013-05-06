package org.pill.repository.local;

import com.google.inject.*;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import com.jolbox.bonecp.BoneCPDataSource;
import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import java.security.ProviderException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.pill.sql.Session;

/**
 * Guice configuration.
 * <p/>
 * @author Gili Tzabari
 */
public class GuiceConfig extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Session.class).toProvider(SessionProvider.class).in(ServletScopes.REQUEST);
		bind(Connection.class).toProvider(ConnectionProvider.class);
		install(new ServletModule());
	}

	@Singleton
	@Provides
	private SQLTemplates getDatabaseDialect()
	{
		return new H2Templates();
	}

	@Singleton
	@Provides
	private DataSource getDataSource(Injector injector)
	{
		BoneCPDataSource result = new BoneCPDataSource();

		// Each test gets its own database that dies once the DataSource gets garbage-collected
		Injector rootInjector = injector;
		while (rootInjector.getParent() != null)
			rootInjector = rootInjector.getParent();
		result.setJdbcUrl("jdbc:h2:mem:" + rootInjector.hashCode()
			+ ";DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4");
		result.setUsername("sa");
		result.setPassword("sa");

		// Prevent the in-memory database from being cleared until the DataSource is garbage-collected
		result.setMinConnectionsPerPartition(1);
		result.setMaxConnectionsPerPartition(20);
		return result;
	}

	/**
	 * Provides a database connection.
	 */
	private static class ConnectionProvider implements Provider<Connection>
	{
		private final DataSource dataSource;

		/**
		 * Creates a new ConnectionProvider.
		 * <p/>
		 * @param dataSource the data source
		 */
		@Inject
		public ConnectionProvider(DataSource dataSource)
		{
			this.dataSource = dataSource;
		}

		@Override
		public Connection get()
		{
			try
			{
				return dataSource.getConnection();
			}
			catch (SQLException e)
			{
				throw new ProvisionException("", e);
			}
		}
	}

	/**
	 * Provides a database Session.
	 */
	@SuppressWarnings("PublicInnerClass")
	public static class SessionProvider implements Provider<Session>
	{
		private final DataSource dataSource;

		/**
		 * Creates a new SessionProvider.
		 * <p/>
		 * @param dataSource the DataSource
		 * @throws NullPointerException if dataSource is null
		 */
		@Inject
		public SessionProvider(DataSource dataSource)
		{
			this.dataSource = dataSource;
		}

		@Override
		public Session get()
		{
			try
			{
				Connection connection = dataSource.getConnection();
				connection.setAutoCommit(false);
				return new Session(connection, new H2Templates());
			}
			catch (SQLException e)
			{
				throw new ProviderException(e);
			}
		}
	}
}
