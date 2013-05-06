package org.pill.repository.local;

import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import java.util.concurrent.Callable;

/**
 * @author Gili Tzabari
 */
public class Testcase
{
	private void oldAPI(String username) throws Exception
	{
		Injector injector = Guice.createInjector(new ServletModule()
		{
			@Override
			protected void configureServlets()
			{
				bind(String.class).annotatedWith(Names.named("username")).to(String.class).
					in(RequestScoped.class);
				bind(Registry.class).to(RegistryImpl.class);
			}
		});
		OldGetUserHome getUserHome = injector.getInstance(OldGetUserHome.class);
		System.out.println("Old result: " + ServletScopes.scopeRequest(getUserHome,
			ImmutableMap.<Key<?>, Object>of(Key.get(String.class,
			Names.named("username")), username)).call());
		injector.getInstance(GuiceFilter.class).destroy();
	}

	private static class OldGetUserHome implements Callable<String>
	{
		private final Provider<String> username;
		private final Provider<Registry> registry;

		@Inject
		public OldGetUserHome(@Named("username") Provider<String> username, Provider<Registry> registry)
		{
			this.username = username;
			this.registry = registry;
		}

		@Override
		public String call() throws Exception
		{
			return registry.get().getUserHome(username.get());
		}
	}

	private void newAPI(final String username) throws Exception
	{
		Injector injector = Guice.createInjector(new ServletModule()
		{
			@Override
			protected void configureServlets()
			{
				bind(Registry.class).to(RegistryImpl.class);
			}
		});
		RequestInjector requestInjector = injector.getInstance(RequestInjector.class);
		System.out.println("New result: " + requestInjector.scopeRequest(NewGetUserHome.class,
			new AbstractModule()
			{
				@Override
				protected void configure()
				{
					bind(String.class).annotatedWith(Names.named("username")).toInstance(username);
				}
			}).call());
		injector.getInstance(GuiceFilter.class).destroy();
	}

	@RequestScoped
	private static class NewGetUserHome implements Callable<String>
	{
		private final String username;
		private final Registry registry;

		@Inject
		public NewGetUserHome(@Named("username") String username, Registry registry)
		{
			this.username = username;
			this.registry = registry;
		}

		@Override
		public String call() throws Exception
		{
			return registry.getUserHome(username);
		}
	}

	private interface Registry
	{
		String getUserHome(String username);
	}

	@RequestScoped
	private static class RegistryImpl implements Registry
	{
		@Override
		public String getUserHome(String username)
		{
			return "/users/" + username;
		}
	}

	public static void main(String[] args) throws Exception
	{
		Testcase testcase = new Testcase();
		testcase.oldAPI("george");
		testcase.newAPI("lucy");
	}
}
