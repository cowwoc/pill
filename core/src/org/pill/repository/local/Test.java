package org.pill.repository.local;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import javax.servlet.ServletException;

/**
 * Tests continuation of requests
 */
public class Test
{
	private static final String A_VALUE = "thereaoskdao";
	private static final String A_DIFFERENT_VALUE = "hiaoskd";
	private static final String SHOULDNEVERBESEEN = "Shouldneverbeseen!";

	public final void testNonHttpRequestScopedCallable()
		throws ServletException, IOException, InterruptedException, ExecutionException
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();

		// We use servlet module here because we want to test that @RequestScoped
		// behaves properly with the non-HTTP request scope logic.
		Injector injector = Guice.createInjector(new ServletModule()
		{
			@Override
			protected void configureServlets()
			{
				bindConstant().annotatedWith(Names.named(SomeObject.INVALID)).to(SHOULDNEVERBESEEN);
				bind(SomeObject.class).annotatedWith(Names.named("name")).to(SomeObject.class).
					in(RequestScoped.class);
			}
		});

		SomeObject someObject = new SomeObject(A_VALUE);
		OffRequestCallable offRequestCallable = injector.getInstance(OffRequestCallable.class);
		ImmutableMap<Key<?>, Object> seedMap = 
			ImmutableMap.<Key<?>, Object>of(Key.get(SomeObject.class), someObject);
		executor.submit(ServletScopes.scopeRequest(offRequestCallable,
			seedMap)).get();

		assert(injector.getInstance(OffRequestCallable.class)==offRequestCallable);

		// Make sure the value was passed on.
		assert(someObject.value.equals(offRequestCallable.value));
		assert(!SHOULDNEVERBESEEN.equals(someObject.value));

		// Now create a new request and assert that the scopes don't cross.
		someObject = new SomeObject(A_DIFFERENT_VALUE);
		executor.submit(ServletScopes.scopeRequest(offRequestCallable,
			ImmutableMap.<Key<?>, Object>of(Key.get(SomeObject.class), someObject))).get();

		assert(injector.getInstance(OffRequestCallable.class).equals(offRequestCallable));

		// Make sure the value was passed on.
		assert(someObject.value.equals(offRequestCallable.value));
		assert(!SHOULDNEVERBESEEN.equals(someObject.value));
		executor.shutdown();
		executor.awaitTermination(2, TimeUnit.SECONDS);
	}

	public final void testWrongValueClasses() throws Exception
	{
		Injector injector = Guice.createInjector(new ServletModule()
		{
			@Override
			protected void configureServlets()
			{
				bindConstant().annotatedWith(Names.named(SomeObject.INVALID)).to(SHOULDNEVERBESEEN);
				bind(SomeObject.class).in(RequestScoped.class);
			}
		});

		OffRequestCallable offRequestCallable = injector.getInstance(OffRequestCallable.class);
		try
		{
			ServletScopes.scopeRequest(offRequestCallable,
				ImmutableMap.<Key<?>, Object>of(Key.get(SomeObject.class), "Boo!"));
			assert(false);
		}
		catch (IllegalArgumentException iae)
		{
			assert (("Value[Boo!] of type[java.lang.String] is not compatible with key["
				+ Key.get(SomeObject.class) + "]").equals(iae.getMessage()));
		}
	}

	public final void testNullReplacement() throws Exception
	{
		Injector injector = Guice.createInjector(new ServletModule()
		{
			@Override
			protected void configureServlets()
			{
				bindConstant().annotatedWith(Names.named(SomeObject.INVALID)).to(SHOULDNEVERBESEEN);
				bind(SomeObject.class).in(RequestScoped.class);
			}
		});

		Callable<SomeObject> callable = injector.getInstance(Caller.class);
		try
		{
			assert (callable.call() != null);
			assert (false);
		}
		catch (ProvisionException pe)
		{
			assert (pe.getCause() instanceof OutOfScopeException);
		}

		// Validate that an actual null entry in the map results in a null injected object.
		Map<Key<?>, Object> map = Maps.newHashMap();
		map.put(Key.get(SomeObject.class), null);
		callable = ServletScopes.scopeRequest(injector.getInstance(Caller.class), map);
		assert(callable.call() == null);
	}

	@RequestScoped
	public static class SomeObject
	{
		private static final String INVALID = "invalid";

		@Inject
		public SomeObject(@Named(INVALID) String value)
		{
			this.value = value;
		}
		private final String value;
	}

	@Singleton
	public static class OffRequestCallable implements Callable<String>
	{
		@Inject @Named("name")
		Provider<SomeObject> someObject;
		public String value;

		public String call() throws Exception
		{
			// Inside this request, we should always get the same instance.
			assert (someObject.get().equals(someObject.get()));

			value = someObject.get().value;
			assert (!SHOULDNEVERBESEEN.equals(value));

			return value;
		}
	}

	private static class Caller implements Callable<SomeObject>
	{
		@Inject
		Provider<SomeObject> someObject;

		public SomeObject call() throws Exception
		{
			return someObject.get();
		}
	}

	public static void main(String[] args) throws ServletException, IOException, InterruptedException, ExecutionException
	{
		Test test = new Test();
		test.testNonHttpRequestScopedCallable();
	}
}
