package com.notifier.desktop.util;

import java.util.concurrent.*;

public class MoreFutures {

	private MoreFutures() {
	}

	public static <V> Future<V> combine(final Future<V> future1, final Future<V> future2) {
		return new Future<V>() {
			@Override
			public boolean isDone() {
				return future1.isDone() && future2.isDone();
			}

			@Override
			public boolean isCancelled() {
				return future1.isCancelled() && future2.isCancelled();
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return future1.cancel(mayInterruptIfRunning) && future2.cancel(mayInterruptIfRunning);
			}

			/**
			 * Returns the result of the future that didn't throw an ExecutionException.
			 * If both futures throw ExecutionException, the exception thrown by future2 will be
			 * thrown by this method.
			 */
			@Override
			public V get() throws InterruptedException, ExecutionException {
				V value;
				try {
					value = future1.get();
					try {
						value = future2.get();
					} catch (ExecutionException e) {
					}
				} catch (ExecutionException e) {
					value = future2.get();
				}
				return value;
			}

			@Override
			public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				V value;
				try {
					value = future1.get(timeout, unit);
					try {
						value = future2.get(timeout, unit);
					} catch (ExecutionException e) {
					}
				} catch (ExecutionException e) {
					value = future2.get(timeout, unit);
				}
				return value;
			}
		};
	}
}
