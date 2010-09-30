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
package com.notifier.desktop.tray;

import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;

import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.app.*;
import com.notifier.desktop.view.*;

public class SwtTrayManager implements TrayManager {

	private @Inject Application application;
	private @Inject NotificationManager notificationManager;
	private @Inject NotificationParser<byte[]> notificationParser;
	private @Inject SwtManager swtManager;
	
	private Image trayImage;
	private TrayItem trayItem;

	@Override
	public boolean start() throws IOException {
		if (swtManager.getDisplay().getSystemTray() == null) {
			return false;
		}

		if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.MAC) {
			trayImage = new Image(swtManager.getDisplay(), Application.class.getResourceAsStream(Application.ICON_NAME_MAC));
		} else {
			trayImage = new Image(swtManager.getDisplay(), Application.class.getResourceAsStream(Application.ICON_NAME));
		}
		Tray tray = swtManager.getDisplay().getSystemTray();
		trayItem = new TrayItem(tray, SWT.NONE);
		trayItem.setToolTipText(Application.NAME);
		trayItem.setImage(trayImage);

		final Menu menu = new Menu(swtManager.getShell(), SWT.POP_UP);
		
		MenuItem preferencesItem = new MenuItem(menu, SWT.PUSH);
		preferencesItem.setText("Preferences...");
		preferencesItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!swtManager.isShowingPreferencesDialog()) {
					PreferencesDialog preferencesDialog = new PreferencesDialog(application, notificationManager, notificationParser, swtManager);
					preferencesDialog.open();
				}
			}
		});

		MenuItem checkUpdatesItem = new MenuItem(menu, SWT.PUSH);
		checkUpdatesItem.setText("Check for Updates");
		checkUpdatesItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				application.checkForUpdates();
			}
		});

		MenuItem openLogDirItem = new MenuItem(menu, SWT.PUSH);
		openLogDirItem.setText("Show Log");
		openLogDirItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Program.launch(OperatingSystems.getWorkDirectory());
			}
		});

		MenuItem aboutItem = new MenuItem(menu, SWT.PUSH);
		aboutItem.setText("About");
		aboutItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!swtManager.isShowingAboutDialog()) {
					Version version = application.getVersion();
					if (version != null) {
						AboutDialog aboutDialog = new AboutDialog(version, swtManager);
						aboutDialog.open();
					}
				}
			}
		});

		MenuItem quitItem = new MenuItem(menu, SWT.PUSH);
		quitItem.setText("Quit");
		quitItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				application.shutdown();
			}
		});

		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});
		
		// Double-click event
		if (OperatingSystems.CURRENT_FAMILY != OperatingSystems.Family.MAC) {
			trayItem.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if (!swtManager.isShowingPreferencesDialog() &&
						OperatingSystems.CURRENT_FAMILY != OperatingSystems.Family.MAC) {
						PreferencesDialog preferencesDialog = new PreferencesDialog(application, notificationManager, notificationParser, swtManager);
						preferencesDialog.open();
					}
				}
			});
		}

		return true;
	}

	@Override
	public void showNotification(final Notification notification) {
		swtManager.update(new Runnable() {
			@Override
			public void run() {
				if (!swtManager.getShell().isDisposed()) {
					ToolTip tip = new ToolTip(swtManager.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
					tip.setText(notification.getTitle(Application.NAME));
					tip.setMessage(notification.getDescription() == null ? "No description" : notification.getDescription());
					trayItem.setToolTip(tip);
					tip.setVisible(true);
				}
			}
		});
	}

	public void stop() {
		if (Thread.currentThread() == swtManager.getDisplay().getThread()) {
			trayItem.dispose();
			trayImage.dispose();
		} else {
			swtManager.update(new Runnable() {
				@Override
				public void run() {
					trayItem.dispose();
					trayImage.dispose();
				}
			});
		}
	}

}
