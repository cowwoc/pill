package org.pill.repository.local;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.googlecode.flyway.core.Flyway;
import com.mysema.query.QueryException;
import com.mysema.query.Tuple;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.pill.Dependency;
import org.pill.DependencyType;
import org.pill.EntityExistsException;
import org.pill.EntityNotFoundException;
import org.pill.Module;
import org.pill.Release;
import org.pill.ReleaseBuilder;
import org.pill.repository.RepositorySpi;
import org.pill.repository.local.queries.QDependencyTypes;
import org.pill.repository.local.queries.QModules;
import org.pill.repository.local.queries.QReleaseDependencies;
import org.pill.repository.local.queries.QReleases;
import org.pill.sql.ConstraintViolationException;
import org.pill.sql.SQLExceptions;
import org.pill.sql.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local repository.
 * <p/>
 * <b>THREAD-SAFETY</b>: This implementation is not thread-safe.
 * <p/>
 * @author Gili Tzabari
 */
@Singleton
public final class LocalRepository implements RepositorySpi
{
	private static LocalRepository instance;
	private static final String schema = LocalRepository.class.getPackage().getName() + ".release";
	private final Path rootDirectory = Paths.get(System.getProperty("user.home"), ".pill");
	private final RequestInjector requestInjector;
	private final Logger log = LoggerFactory.getLogger(LocalRepository.class);

	/**
	 * Creates a new LocalRepository.
	 * <p/>
	 * @param dataSource the database associated with the repository
	 * @param requestInjector executes code in request scope
	 * @throws NullPointerException if dataSource or requestInjector are null
	 */
	@Inject
	private LocalRepository(DataSource dataSource, RequestInjector requestInjector)
	{
		Preconditions.checkNotNull(dataSource, "dataSource may not be null");
		Preconditions.checkNotNull(requestInjector, "requestInjector may not be null");

		this.requestInjector = requestInjector;
		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);
		flyway.setLocations("org/pill/database/migration");
		flyway.migrate();
	}

	/**
	 * @return the local repository
	 */
	public static synchronized LocalRepository getInstance()
	{
		if (instance == null)
		{
			Injector injector = Guice.createInjector(new GuiceConfig());
			instance = injector.getInstance(LocalRepository.class);
		}
		return instance;
	}

	@Override
	public Module insertModule(final String name)
		throws EntityExistsException, IOException
	{
		Preconditions.checkNotNull(name, "name may not be null");
		Preconditions.checkArgument(!name.isEmpty(), "name may not be an empty string");
		try
		{
			return requestInjector.scopeRequest(InsertModule.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(String.class).annotatedWith(Names.named("name")).toInstance(name);
				}
			}).call();
		}
		catch (IOException | EntityExistsException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements insertModule().
	 */
	@RequestScoped
	private static class InsertModule implements Callable<Module>
	{
		private final String name;
		private final Session session;

		@Inject
		public InsertModule(@Named("name") String name, Session session)
		{
			this.name = name;
			this.session = session;
		}

		@Override
		public Module call() throws IOException, EntityExistsException
		{
			try
			{
				Long id;
				try
				{
					QModules modules = QModules.modules;
					id = session.insert(modules).set(modules.name, name).executeWithKey(modules.id);
					if (id == null)
						throw new IOException("Could not insert module");
					session.commit();
				}
				catch (QueryException e)
				{
					ConstraintViolationException constraintViolation = SQLExceptions.getConstraintViolation(e);
					if (constraintViolation != null)
						throw new EntityExistsException(name, e);
					throw new IOException(e);
				}
				return new Module(name);
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	public ReleaseBuilder insertRelease(Module module, String version, Path path)
	{
		return new ReleaseBuilder(this, module, version, path);
	}

	@Override
	public Release insertRelease(final Module module, final String version,
		final Path path, final Set<Dependency> dependencies)
		throws EntityExistsException, IOException
	{
		Preconditions.checkNotNull(module, "module may not be null");
		Preconditions.checkNotNull(version, "version may not be null");
		Preconditions.checkArgument(!version.isEmpty(), "version may not be an empty string");
		Preconditions.checkNotNull(path, "path may not be null");
		Preconditions.checkNotNull(dependencies, "dependencies may not be null");
		try
		{
			return requestInjector.scopeRequest(InsertRelease.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(Module.class).annotatedWith(Names.named("module")).toInstance(module);
					bind(String.class).annotatedWith(Names.named("version")).toInstance(version);
					bind(Path.class).annotatedWith(Names.named("path")).toInstance(path);
					bind(new TypeLiteral<Set<Dependency>>()
					{
					}).annotatedWith(Names.named("dependencies")).toInstance(dependencies);
					bind(Logger.class).annotatedWith(Names.named("log")).toInstance(log);
				}
			}).call();
		}
		catch (IllegalArgumentException | IOException | EntityExistsException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements insertRelease().
	 */
	@RequestScoped
	private static class InsertRelease implements Callable<Release>
	{
		private final Path path;
		private final Module module;
		private final String version;
		private final Set<Dependency> dependencies;
		private final Session session;

		@Inject
		public InsertRelease(@Named("path") Path path,
			@Named("module") Module module,
			@Named("version") String version,
			@Named("dependencies") Set<Dependency> dependencies,
			Session session)
		{
			this.path = path;
			this.module = module;
			this.version = version;
			this.dependencies = dependencies;
			this.session = session;
		}

		@Override
		public Release call() throws IllegalArgumentException, IOException, EntityExistsException
		{
			try
			{
				Long releaseId;
				try
				{
					QModules modules = QModules.modules;
					Long moduleId = session.query(modules).
						where(modules.name.eq(module.getName())).uniqueResult(modules.id);
					if (moduleId == null)
						throw new IllegalArgumentException("Module " + module.getName() + " not found");

					Timestamp lastModified = new Timestamp(Files.readAttributes(path,
						BasicFileAttributes.class).lastModifiedTime().toMillis());
					SerialBlob content;
					try
					{
						content = new SerialBlob(Files.readAllBytes(path));
					}
					catch (SQLException e)
					{
						throw new QueryException(e);
					}
					QReleases releases = QReleases.releases;
					releaseId = session.insert(releases).
						set(releases.moduleId, moduleId).
						set(releases.version, version).
						set(releases.path, path.getFileName().toString()).
						set(releases.content, content).
						set(releases.lastModified, lastModified).
						executeWithKey(releases.id);
					if (releaseId == null)
						throw new IOException("Could not insert the release");

					Map<String, Byte> dependencyTypes = new HashMap<>();
					QDependencyTypes dt = QDependencyTypes.dependencyTypes;
					for (Tuple row : session.query(dt).list(dt.id, dt.name))
					{
						Byte id = row.get(dt.id);
						String name = row.get(dt.name);
						dependencyTypes.put(name, id);
					}

					QReleaseDependencies rd = QReleaseDependencies.releaseDependencies;
					for (Dependency dependency : dependencies)
					{
						long rows = session.insert(rd).set(rd.releaseId, releaseId).
							set(rd.module, module.getName()).
							set(rd.version, version).
							set(rd.type, dependencyTypes.get(dependency.getType().name())).
							set(rd.uri, dependency.getUri().toString()).
							execute();
						if (rows != 1)
							throw new IOException("Could not insert dependency: " + dependency);
					}
					session.commit();
				}
				catch (QueryException e)
				{
					ConstraintViolationException constraintViolation = SQLExceptions.getConstraintViolation(e);
					if (constraintViolation != null)
						throw new EntityExistsException(module.getName() + " " + version, e);
					throw new IOException(e);
				}
				return new Release(toUri(releaseId), module, version, path.getFileName().toString(),
					dependencies);
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	public Module getModule(final String name) throws IOException
	{
		Preconditions.checkNotNull(name, "name may not be null");
		Preconditions.checkArgument(!name.isEmpty(), "name may not be an empty string");
		try
		{
			return requestInjector.scopeRequest(GetModule.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(String.class).annotatedWith(Names.named("name")).toInstance(name);
				}
			}).call();
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements getModule().
	 */
	@RequestScoped
	private static class GetModule implements Callable<Module>
	{
		private final String name;
		private final Session session;

		@Inject
		public GetModule(@Named("name") String name, Session session)
		{
			this.name = name;
			this.session = session;
		}

		@Override
		public Module call() throws IOException
		{
			try
			{
				QModules modules = QModules.modules;
				Long id = session.query(modules).where(modules.name.eq(name)).uniqueResult(modules.id);
				if (id == null)
					return null;
				return new Module(name);
			}
			catch (QueryException e)
			{
				throw new IOException(e);
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	public List<URI> getReleases(final Module module)
		throws IOException
	{
		Preconditions.checkNotNull(module, "module may not be null");
		try
		{
			return requestInjector.scopeRequest(GetReleases.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(Module.class).annotatedWith(Names.named("module")).toInstance(module);
					bind(Logger.class).annotatedWith(Names.named("log")).toInstance(log);
				}
			}).call();
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements getReleases().
	 */
	@RequestScoped
	private static class GetReleases implements Callable<List<URI>>
	{
		private final Module module;
		private final Session session;

		@Inject
		public GetReleases(@Named("module") Module module, @Named("log") Logger log, Session session)
		{
			this.module = module;
			this.session = session;
		}

		@Override
		public List<URI> call() throws IOException
		{
			try
			{
				QModules modules = QModules.modules;
				Long moduleId = session.query(modules).where(modules.name.eq(module.getName())).
					uniqueResult(modules.id);
				if (moduleId == null)
					return null;

				List<URI> result = new ArrayList<>();
				QReleases releases = QReleases.releases;
				for (Long id : session.query(releases).where(releases.moduleId.eq(moduleId)).
					list(releases.id))
				{
					result.add(URI.create(schema + ":" + id));
				}
				return result;
			}
			finally
			{
				session.close();
			}
		}
	}

	/**
	 * Converts a URI to a database identifier.
	 * <p/>
	 * @param uri the uri
	 * @return the database identifier
	 * @throws IllegalArgumentException if uri is not a valid module or release
	 */
	private static long toId(URI uri)
	{
		// Releases are identified by "<package>.release:<id>" where <package> is the current package
		// and <id> is the database identifier.
		if (!uri.getScheme().startsWith(schema))
			throw new IllegalArgumentException(uri.toString());
		String schemeSpecific = uri.getSchemeSpecificPart();
		int endIndex = schemeSpecific.indexOf(':');
		if (endIndex != -1)
			throw new IllegalArgumentException("Invalid uri: " + uri);
		endIndex = schemeSpecific.length();
		try
		{
			return Long.parseLong(schemeSpecific.substring(0, endIndex));
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Converts a database identifier to an URI.
	 * <p/>
	 * @param id the database identifier
	 * @return the URI
	 */
	private static URI toUri(long id)
	{
		return URI.create(schema + ":" + id);
	}

	@Override
	public URI getReleaseUri(final Module module, final String version)
	{
		Preconditions.checkNotNull(module, "module may not be null");
		Preconditions.checkNotNull(version, "version may not be null");
		Preconditions.checkArgument(!version.isEmpty(), "version may not be an empty string");
		try
		{
			return requestInjector.scopeRequest(GetReleaseUri.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(Module.class).annotatedWith(Names.named("module")).toInstance(module);
					bind(String.class).annotatedWith(Names.named("version")).toInstance(version);
					bind(Logger.class).annotatedWith(Names.named("log")).toInstance(log);
				}
			}).call();
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements getReleaseUri().
	 */
	@RequestScoped
	private static class GetReleaseUri implements Callable<URI>
	{
		private final Module module;
		private final String version;
		private final Logger log;
		private final Session session;

		@Inject
		public GetReleaseUri(@Named("module") Module module, @Named("version") String version,
			@Named("log") Logger log, Session session)
		{
			this.module = module;
			this.version = version;
			this.session = session;
			this.log = log;
		}

		@Override
		public URI call() throws IOException
		{
			try
			{
				QModules modules = QModules.modules;
				Long moduleId = session.query(modules).where(modules.name.eq(module.getName())).
					uniqueResult(modules.id);
				if (moduleId == null)
					return null;

				QReleases releases = QReleases.releases;
				Long id = session.query(releases).
					where(releases.moduleId.eq(moduleId), releases.version.eq(version)).
					uniqueResult(releases.id);
				if (id == null)
					return null;
				return toUri(id);
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	public Release getRelease(final URI uri)
	{
		try
		{
			return requestInjector.scopeRequest(GetReleaseById.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(URI.class).annotatedWith(Names.named("uri")).toInstance(uri);
					bind(Logger.class).annotatedWith(Names.named("log")).toInstance(log);
				}
			}).call();
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements getReleaseById().
	 */
	@RequestScoped
	private static class GetReleaseById implements Callable<Release>
	{
		private final URI uri;
		private final Logger log;
		private final Session session;

		@Inject
		public GetReleaseById(@Named("uri") URI uri, @Named("log") Logger log, Session session)
		{
			this.uri = uri;
			this.log = log;
			this.session = session;
		}

		@Override
		public Release call() throws IOException
		{
			try
			{
				long id = toId(uri);
				QModules modules = QModules.modules;
				QReleases releases = QReleases.releases;

				Tuple row = session.query(modules, releases).
					where(releases.id.eq(id), modules.id.eq(releases.moduleId)).
					uniqueResult(modules.name, releases.version, releases.path);
				if (row == null)
					return null;
				Module module = new Module(row.get(modules.name));
				String version = row.get(releases.version);
				String filename = row.get(releases.path);
				Set<Dependency> dependencies = getDependencies(id);
				return new Release(uri, module, version, filename, dependencies);
			}
			finally
			{
				session.close();
			}
		}

		/**
		 * @param id a release id
		 * @return the set of dependencies associated with the release
		 */
		private Set<Dependency> getDependencies(long id)
		{
			Set<Dependency> result = new HashSet<>();
			QReleaseDependencies dependencies = QReleaseDependencies.releaseDependencies;
			QDependencyTypes dt = QDependencyTypes.dependencyTypes;
			for (Tuple row : session.query(dependencies, dt).
				where(dependencies.releaseId.eq(id), dependencies.type.eq(dt.id)).
				list(dependencies.module, dependencies.version, dt.name, dependencies.uri))
			{
				Module module = new Module(row.get(dependencies.module));
				String version = row.get(dependencies.version);
				DependencyType type = DependencyType.valueOf(row.get(dt.name));
				URI dependencyUri = URI.create(row.get(dependencies.uri));
				result.add(new Dependency(dependencyUri, module, version, type));
			}
			return result;
		}
	}

	/**
	 * Returns the release id associated with a URI.
	 * <p/>
	 * @param uri a ReleasePath URI
	 * @return the release id
	 * @throws ParseException if the URI did not correspond to a release
	 */
	private long parseReleaseId(URI uri) throws ParseException
	{
		if (!uri.getScheme().equals(schema))
			throw new ParseException("Expected " + schema + ", got: " + uri.getScheme(), 0);
		try
		{
			String schemeSpecificPart = uri.getSchemeSpecificPart();
			int index = schemeSpecificPart.indexOf(":");
			if (index == -1)
				throw new ParseException("Missing colon in scheme-specific psrt", uri.toString().length());
			return Long.parseLong(schemeSpecificPart.substring(0, index));
		}
		catch (NumberFormatException e)
		{
			ParseException e2 = new ParseException("Invalid release id", uri.getScheme().length() + ":".
				length() + 1);
			e2.initCause(e);
			throw e2;
		}
	}

	@Override
	public SeekableByteChannel newByteChannel(final URI uri) throws NoSuchFileException, IOException
	{
		try
		{
			return requestInjector.scopeRequest(NewByteChannel.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					try
					{
						bind(Long.class).annotatedWith(Names.named("releaseId")).toInstance(parseReleaseId(uri));
					}
					catch (ParseException e)
					{
						throw new IllegalArgumentException(e);
					}
				}
			}).call();
		}
		catch (NoSuchFileException e)
		{
			throw e;
		}
		catch (QueryException e)
		{
			throw new IOException(e);
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements newByteChannel().
	 */
	@RequestScoped
	private static class NewByteChannel implements Callable<SeekableByteChannel>
	{
		private final Long releaseId;
		private final Session session;

		@Inject
		public NewByteChannel(@Named("releaseId") Long releaseId, Session session)
		{
			this.releaseId = releaseId;
			this.session = session;
		}

		@Override
		public SeekableByteChannel call() throws NoSuchFileException, QueryException
		{
			try
			{
				QReleases releases = QReleases.releases;
				Blob content = session.query(releases).where(releases.id.eq(releaseId)).
					uniqueResult(releases.content);
				if (content == null)
					throw new NoSuchFileException("release #" + releaseId);
				// NOTE: Session closed by BlobByteChannel.close()
				return new BlobByteChannel(Collections.singleton(StandardOpenOption.READ), content, session);
			}
			catch (RuntimeException e)
			{
				session.close();
				throw e;
			}
		}
	}

	@Override
	public boolean fileExists(final URI uri) throws IOException
	{
		try
		{
			return requestInjector.scopeRequest(FileExists.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					try
					{
						bind(Long.class).annotatedWith(Names.named("releaseId")).toInstance(parseReleaseId(uri));
					}
					catch (ParseException e)
					{
						throw new IllegalArgumentException(e);
					}
				}
			}).call();
		}
		catch (QueryException e)
		{
			throw new IOException(e);
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements fileExists().
	 */
	@RequestScoped
	private static class FileExists implements Callable<Boolean>
	{
		private final Long releaseId;
		private final String path;
		private final Session session;

		@Inject
		public FileExists(@Named("releaseId") Long releaseId, @Named("path") String path,
			Session session)
		{
			this.releaseId = releaseId;
			this.path = path;
			this.session = session;
		}

		@Override
		public Boolean call() throws QueryException
		{
			try
			{
				QReleases releases = QReleases.releases;
				return session.query(releases).where(releases.id.eq(releaseId), releases.path.eq(path)).
					exists();
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	public BasicFileAttributes readAttributes(final URI uri) throws NoSuchFileException, IOException
	{
		try
		{
			return requestInjector.scopeRequest(ReadAttributes.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					try
					{
						bind(Long.class).annotatedWith(Names.named("releaseId")).toInstance(parseReleaseId(uri));
					}
					catch (ParseException e)
					{
						throw new IllegalArgumentException(e);
					}
				}
			}).call();
		}
		catch (NoSuchFileException e)
		{
			throw e;
		}
		catch (QueryException e)
		{
			throw new IOException(e);
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements readAttributes().
	 */
	@RequestScoped
	private static class ReadAttributes implements Callable<BasicFileAttributes>
	{
		private final Long releaseId;
		private final Session session;

		@Inject
		public ReadAttributes(@Named("releaseId") Long releaseId, Session session)
		{
			this.releaseId = releaseId;
			this.session = session;
		}

		@Override
		public BasicFileAttributes call() throws NoSuchFileException, QueryException
		{
			try
			{
				QReleases releases = QReleases.releases;
				Tuple row = session.query(releases).where(releases.id.eq(releaseId)).
					uniqueResult(releases.path, releases.lastModified, releases.content);
				if (row == null)
					throw new NoSuchFileException("Release #" + releaseId);
				String path = row.get(releases.path);
				DateTime lastModified = new DateTime(row.get(releases.lastModified), DateTimeZone.UTC);
				Blob content = row.get(releases.content);
				long size;
				try
				{
					try
					{
						size = content.length();
					}
					catch (SQLException e)
					{
						throw new QueryException(e);
					}
				}
				finally
				{
					try
					{
						content.free();
					}
					catch (SQLException e)
					{
						throw new QueryException(e);
					}
				}
				return new ReleaseFileAttributes(path, size, FileTime.fromMillis(lastModified.getMillis()));
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	public void removeModule(final Module module) throws IOException, EntityNotFoundException
	{
		Preconditions.checkNotNull(module, "module may not be null");
		try
		{
			requestInjector.scopeRequest(RemoveModule.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(Module.class).annotatedWith(Names.named("module")).toInstance(module);
				}
			}).call();
		}
		catch (IllegalStateException | EntityNotFoundException | IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements removeModule().
	 */
	@RequestScoped
	private static class RemoveModule implements Callable<Void>
	{
		private final Module module;
		private final Session session;

		@Inject
		public RemoveModule(@Named("module") Module module, Session session)
		{
			this.module = module;
			this.session = session;
		}

		@Override
		public Void call() throws IllegalStateException, EntityNotFoundException, IOException
		{
			try
			{
				QModules modules = QModules.modules;
				Long moduleId = session.query(modules).where(modules.name.eq(module.getName())).
					uniqueResult(modules.id);
				if (moduleId == null)
					throw new EntityNotFoundException(module.getName());

				QReleases releases = QReleases.releases;
				long releaseCount = session.query(releases).where(releases.moduleId.eq(moduleId)).
					count();
				if (releaseCount > 0)
				{
					throw new IllegalStateException("Module contains " + releaseCount
						+ " versions which must be removed first");
				}

				long rows = session.delete(modules).where(modules.id.eq(moduleId)).execute();
				if (rows == 0)
					throw new EntityNotFoundException(module.getName());
				if (rows != 1)
					throw new AssertionError("Expected to delete 1 row, found " + rows + " rows instead");
				session.commit();
				return null;
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	public void removeRelease(final Release release) throws IOException, EntityNotFoundException
	{
		Preconditions.checkNotNull(release, "release may not be null");
		try
		{
			requestInjector.scopeRequest(RemoveRelease.class, new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(Release.class).annotatedWith(Names.named("release")).toInstance(release);
				}
			}).call();
		}
		catch (IOException | EntityNotFoundException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Implements removeRelease().
	 */
	@RequestScoped
	private static class RemoveRelease implements Callable<Void>
	{
		private final Release release;
		private final Session session;

		@Inject
		public RemoveRelease(@Named("release") Release release, Session session)
		{
			this.release = release;
			this.session = session;
		}

		@Override
		public Void call() throws IOException, EntityNotFoundException
		{
			try
			{
				long releaseId = toId(release.getUri());
				QReleaseDependencies dependencies = QReleaseDependencies.releaseDependencies;
				session.delete(dependencies).where(dependencies.releaseId.eq(releaseId)).
					execute();

				QReleases releases = QReleases.releases;
				long rows = session.delete(releases).where(releases.id.eq(releaseId)).
					execute();
				if (rows == 0)
					throw new EntityNotFoundException(release.toString());
				if (rows != 1)
					throw new AssertionError("Expected to delete 1 row, found " + rows + " rows instead");
				session.commit();
				return null;
			}
			finally
			{
				session.close();
			}
		}
	}

	@Override
	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
	public boolean equals(Object o)
	{
		if (!(o instanceof LocalRepository))
			return false;
		final LocalRepository other = (LocalRepository) o;
		return this.rootDirectory.equals(other.rootDirectory);
	}

	@Override
	public int hashCode()
	{
		return rootDirectory.hashCode();
	}
}
