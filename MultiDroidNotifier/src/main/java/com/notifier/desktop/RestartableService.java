package com.notifier.desktop;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import com.google.common.base.*;

import static com.google.common.base.Preconditions.*;

/**
 * Based on com.google.common.util.concurrent.AbstractService.
 */
public abstract class RestartableService implements Service {

	private final ReentrantLock lock = new ReentrantLock();

	private Transition startup = new Transition();
	private Transition shutdown = new Transition();

	private State state = State.NEW;
	private boolean shutdownWhenStartupFinishes = false;
	private boolean startupWhenShutdownFinishes = false;
	private boolean autoAcknowledgeStateTransitions;

	public RestartableService() {
		this(true);
	}

	public RestartableService(boolean autoAcknowledgeStateTransitions) {
		this.autoAcknowledgeStateTransitions = autoAcknowledgeStateTransitions;
	}

	protected void doStart() throws Exception {
		// Default behaviour is to do nothing
	}

	protected void doStop() throws Exception {
		// Default behaviour is to do nothing
	}

	public Future<State> start() {
		lock.lock();
		try {
			if (state == State.NEW || state == State.FAILED || state == State.TERMINATED) {
				state = State.STARTING;
				startup = new Transition();
				shutdown = new Transition();
				startupWhenShutdownFinishes = false;
				doStart();
				if (autoAcknowledgeStateTransitions) {
					notifyStarted();
				}
			} else if (state == State.STOPPING) {
				startupWhenShutdownFinishes = true;
			}
		} catch (Throwable startupFailure) {
			// put the exception in the future, the user can get it via Future.get()
			notifyFailed(startupFailure);
		} finally {
			lock.unlock();
		}

		return startup;
	}

	public Future<State> stop() {
		lock.lock();
		try {
			if (state == State.NEW) {
				state = State.TERMINATED;
				startup.transitionSucceeded(State.TERMINATED);
				shutdown.transitionSucceeded(State.TERMINATED);
			} else if (state == State.STARTING) {
				shutdownWhenStartupFinishes = true;
				startup.transitionSucceeded(State.STOPPING);
			} else if (state == State.RUNNING) {
				state = State.STOPPING;
				shutdownWhenStartupFinishes = false;
				doStop();
				if (autoAcknowledgeStateTransitions) {
					notifyStopped();
				}
			}
		} catch (Throwable shutdownFailure) {
			// put the exception in the future, the user can get it via Future.get()
			notifyFailed(shutdownFailure);
		} finally {
			lock.unlock();
		}

		return shutdown;
	}

	public State startAndWait() {
		try {
			return start().get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw Throwables.propagate(e.getCause());
		}
	}

	public State stopAndWait() {
		try {
			return stop().get();
		} catch (ExecutionException e) {
			throw Throwables.propagate(e.getCause());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	protected void notifyStarted() {
		lock.lock();
		try {
			if (state != State.STARTING) {
				IllegalStateException failure = new IllegalStateException("Cannot notifyStarted() when the service is " + state);
				notifyFailed(failure);
				throw failure;
			}

			state = State.RUNNING;
			if (shutdownWhenStartupFinishes) {
				stop();
			} else {
				startup.transitionSucceeded(State.RUNNING);
			}
		} finally {
			lock.unlock();
		}
	}

	protected void notifyStopped() {
		lock.lock();
		try {
			if (state != State.STOPPING && state != State.RUNNING) {
				IllegalStateException failure = new IllegalStateException("Cannot notifyStopped() when the service is " + state);
				notifyFailed(failure);
				throw failure;
			}

			state = State.TERMINATED;
			shutdown.transitionSucceeded(State.TERMINATED);
			if (startupWhenShutdownFinishes) {
				start();
			}
		} finally {
			lock.unlock();
		}
	}

	protected void notifyFailed(Throwable cause) {
		checkNotNull(cause);

		lock.lock();
		try {
			if (state == State.STARTING) {
				startup.transitionFailed(cause);
				shutdown.transitionFailed(new Exception("Service failed to start.", cause));
			} else if (state == State.STOPPING) {
				shutdown.transitionFailed(cause);
			}

			state = State.FAILED;
		} finally {
			lock.unlock();
		}
	}

	public boolean isRunning() {
		return state() == State.RUNNING;
	}

	public State state() {
		lock.lock();
		try {
			if (shutdownWhenStartupFinishes && state == State.STARTING) {
				return State.STOPPING;
			} else {
				return state;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + state() + "]";
	}

	private class Transition implements Future<State> {
		private final CountDownLatch done = new CountDownLatch(1);
		private State result;
		private Throwable failureCause;

		void transitionSucceeded(State resultToSet) {
			// guarded by AbstractService.lock
			this.result = resultToSet;
			done.countDown();
		}

		void transitionFailed(Throwable cause) {
			// guarded by AbstractService.lock
			this.result = State.FAILED;
			this.failureCause = cause;
			done.countDown();
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		public boolean isCancelled() {
			return false;
		}

		public boolean isDone() {
			return done.getCount() == 0;
		}

		public State get() throws InterruptedException, ExecutionException {
			done.await();
			return getImmediately();
		}

		public State get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			if (done.await(timeout, unit)) {
				return getImmediately();
			}
			throw new TimeoutException(RestartableService.this.toString());
		}

		private State getImmediately() throws ExecutionException {
			if (result == State.FAILED) {
				throw new ExecutionException(failureCause);
			} else {
				return result;
			}
		}
	}
}
