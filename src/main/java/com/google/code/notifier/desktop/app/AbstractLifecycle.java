/*
 * Android Notifier Desktop is a multiplatform remote notification client for Android devices.
 *
 * Copyright (C) 2010  Leandro Aparecido
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.google.code.notifier.desktop.app;

import java.util.concurrent.atomic.*;

import com.google.code.notifier.desktop.*;
import com.google.inject.*;

public abstract class AbstractLifecycle implements Lifecycle {

	private @Inject Application application;
	private AtomicBoolean running;

	public AbstractLifecycle() {
		running = new AtomicBoolean();
	}

	@Override
	public void start() throws Exception {
		doStart();
		running.set(true);
	}

	@Override
	public void stop() {
		doStop();
		running.set(false);
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	protected void doStart() throws Exception {
		// Do nothing
	}

	protected void doStop() {
		// Do nothing
	}

	public Application getApplication() {
		return application;
	}
}
