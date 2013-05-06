package org.pill.repository.local;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import java.io.IOException;
import java.security.ProviderException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
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

//	@Singleton
//	@Provides
//	private ReleaseFileSystemProvider getReleaseFileSystemProvider()
//	{
//		for (FileSystemProvider provider: FileSystemProvider.installedProviders())
//		{
//			if (provider.getScheme().equals(getClass().getPackage().getName() + ".release"))
//				return (ReleaseFileSystemProvider) provider;
//		}
//		throw new ProvisionException("Could not find ReleaseFileSystemProvider");
//	}
	@Singleton
	@Provides
	private SQLTemplates getDatabaseDialect()
	{
		return new H2Templates();
	}

	@Singleton
	@Provides
	private DataSource getDataSource() throws IOException
	{
		JdbcDataSource result = new JdbcDataSource();
		result.setURL("jdbc:h2:pill;TRACE_LEVEL_FILE=4");
		result.setUser("sa");
		result.setPassword("sa");
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
