package org.pill.repository.local;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.ServletScopes;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Injects a Callable into a non-HTTP request scope.
 * <p/>
 * @author Gili Tzabari
 */
public final class RequestInjector
{
	private final Map<Key<?>, Object> seedMap = Collections.emptyMap();
	private final Injector injector;

	/**
	 * Creates a new RequestInjector.
	 */
	@Inject
	private RequestInjector(Injector injector)
	{
		this.injector = injector;
	}

	/**
	 * Scopes a Callable in a non-HTTP request scope.
	 * <p/>
	 * @param <V> the type of object returned by the Callable
	 * @param callable the class to inject and execute in the request scope
	 * @param modules additional modules to install into the request scope
	 * @return a wrapper that invokes delegate in the request scope
	 */
	public <V> Callable<V> scopeRequest(final Class<? extends Callable<V>> callable,
		final Module... modules)
	{
		Preconditions.checkNotNull(callable, "callable may not be null");

		return ServletScopes.scopeRequest(new Callable<V>()
		{
			@Override
			public V call() throws Exception
			{
				return injector.createChildInjector(modules).getInstance(callable).call();
			}
		}, seedMap);
	}
}
