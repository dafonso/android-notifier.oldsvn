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
package com.google.code.notifier.desktop.view;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import com.google.code.notifier.desktop.*;

public class Dialogs {

	private Dialogs() {
	}

	public static void centerDialog(Shell shell) {
		Display display = shell.getDisplay();
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}

	public static void bringToForeground(Shell shell) {
		shell.forceActive();
	}

	public static int showError(final SwtManager swtManager, final String title, final String message, boolean later) {
		return showDialog(swtManager, title, message, later, SWT.OK | SWT.ICON_ERROR);
	}

	public static int showInfo(final SwtManager swtManager, final String title, final String message, boolean later) {
		return showDialog(swtManager, title, message, later, SWT.OK | SWT.ICON_INFORMATION);
	}

	private static int showDialog(final SwtManager swtManager, final String title, final String message, boolean later, final int style) {
		if (later) {
			swtManager.update(new Runnable() {
				@Override
				public void run() {
					showDialogInternal(swtManager, title, message, style);
				}
			});
			return -1;
		} else {
			return showDialogInternal(swtManager, title, message, style);
		}
	}

	private static int showDialogInternal(SwtManager swtManager, String title, String message, int style) {
		if (!swtManager.getShell().isDisposed()) {
			MessageBox box = new MessageBox(swtManager.getShell(), style | SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL);
			box.setText(title);
			box.setMessage(message);
			return box.open();
		}
		return -1;
	}
}
