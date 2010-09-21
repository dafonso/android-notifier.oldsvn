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
package com.google.code.notifier.desktop.tray;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.imageio.*;

import com.google.code.notifier.desktop.*;
import com.google.inject.*;

public class AwtTrayManager implements TrayManager {

	@Inject
	private Application application;
	private TrayIcon trayIcon;

	@Override
	public boolean start() throws IOException {
		if (!SystemTray.isSupported()) {
			return false;
		}
		Image icon = ImageIO.read(TrayManager.class.getResourceAsStream(Application.ICON_NAME));
		trayIcon = new TrayIcon(icon, Application.NAME, createMenu());
		trayIcon.setImageAutoSize(true);
		try {
			SystemTray.getSystemTray().add(trayIcon);
		} catch (AWTException e) {
			return false;
		}

		return true;
	}

	@Override
	public void showNotification(Notification notification) {
		trayIcon.displayMessage("Android Notify", notification.getDescription(), TrayIcon.MessageType.INFO);
	}

	@Override
	public void stop() {
		SystemTray.getSystemTray().remove(trayIcon);
	}

	protected PopupMenu createMenu() {
		PopupMenu popup = new PopupMenu();
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				application.shutdown();
			}
		});
		popup.add(exitItem);

		return popup;
	}

}
