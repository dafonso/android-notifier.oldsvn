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
package com.notifier.desktop;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface SwtManager {

	void start();

	void runEventLoop();

	void update(Runnable runnable);

	void stop();

	void registerResourceUser(Widget widget);

	Font getFont(String name, int size, int style, boolean strikeout, boolean underline);

	void sendTextToClipboard(String text);

	void setShowingPreferencesDialog(boolean value);

	void setShowingAboutDialog(boolean value);

	boolean isShowingPreferencesDialog();

	boolean isShowingAboutDialog();

	Display getDisplay();

	Shell getShell();

}
