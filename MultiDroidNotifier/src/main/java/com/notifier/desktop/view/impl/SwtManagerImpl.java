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
package com.notifier.desktop.view.impl;

import java.util.*;
import java.util.List;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import com.google.common.collect.*;
import com.google.inject.*;
import com.notifier.desktop.os.*;
import com.notifier.desktop.view.*;

@Singleton
public class SwtManagerImpl implements SwtManager {

	private Display display;
	private Shell shell;
	private Clipboard clipboard;
	private Map<String, Resource> resources;
	private List<Widget> users;

	private boolean showingPreferencesDialog;
	private boolean showingAboutDialog;

	@Override
	public void start() {
		display = new Display();
		shell = new Shell(display);
		clipboard = new Clipboard(display);
		resources = Maps.newHashMap();
		users = Lists.newArrayList();
	}

	@Override
	public void runEventLoop() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	@Override
	public void update(Runnable runnable) {
		display.asyncExec(runnable);
	}

	@Override
	public void registerResourceUser(Widget widget) {
		if (users.contains(widget)) {
			return;
		}
		users.add(widget);
		widget.addDisposeListener(disposeListener);
	}

	@Override
	public Font getFont(String name, int size, int style, boolean strikeout, boolean underline) {
		String fontName = name + "|" + size + "|" + style + "|" + strikeout + "|" + underline;
		if (resources.containsKey(fontName)) {
			return (Font) resources.get(fontName);
		}
		FontData fd = new FontData(name, size, style);
		if (strikeout || underline) {
			try {
				Class<?> lfCls = Class.forName("org.eclipse.swt.internal.win32.LOGFONT");
				Object lf = FontData.class.getField("data").get(fd);
				if (lf != null && lfCls != null) {
					if (strikeout) {
						lfCls.getField("lfStrikeOut").set(lf, new Byte((byte) 1));
					}
					if (underline) {
						lfCls.getField("lfUnderline").set(lf, new Byte((byte) 1));
					}
				}
			} catch (Throwable e) {
				System.err.println("Unable to set underline or strikeout" + " (probably on a non-Windows platform). " + e);
			}
		}
		Font font = new Font(Display.getDefault(), fd);
		resources.put(fontName, font);
		return font;
	}

	@Override
	public void sendTextToClipboard(final String text) {
		update(new Runnable() {
			@Override
			public void run() {
				if (!clipboard.isDisposed()) {
					TextTransfer transfer = TextTransfer.getInstance();
					String textToSend = OperatingSystems.convertLineDelimiters(text);
					clipboard.setContents(new Object[] { textToSend }, new Transfer[] { transfer });
				}
			}
		});
	}

	@Override
	public void stop() {
		if (Thread.currentThread() == display.getThread()) {
			clipboard.dispose();
			shell.dispose();
			display.dispose();
		} else {
			update(new Runnable() {
				@Override
				public void run() {
					clipboard.dispose();
					shell.dispose();
					display.dispose();
				}
			});
		}
	}

	@Override
	public void setShowingPreferencesDialog(boolean value) {
		showingPreferencesDialog = value;
	}

	@Override
	public void setShowingAboutDialog(boolean value) {
		showingAboutDialog = value;
	}

	@Override
	public boolean isShowingPreferencesDialog() {
		return showingPreferencesDialog;
	}

	@Override
	public boolean isShowingAboutDialog() {
		return showingAboutDialog;
	}

	protected void dispose() {
		for (Map.Entry<String, Resource> entry : resources.entrySet()) {
			entry.getValue().dispose();
		}
		resources.clear();
	}

	@Override
	public Display getDisplay() {
		return display;
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			users.remove(e.getSource());
			if (users.size() == 0) {
				dispose();
			}
		}
	};
}
