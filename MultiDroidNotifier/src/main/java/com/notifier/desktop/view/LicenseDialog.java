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
package com.notifier.desktop.view;

import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.slf4j.*;

import com.google.common.io.*;
import com.notifier.desktop.*;

public class LicenseDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(LicenseDialog.class);

	private Shell dialogShell;
	private Text licenseTextArea;

	public LicenseDialog(Shell parent) {
		super(parent, SWT.NULL);
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

			dialogShell.setLayout(new FormLayout());
			dialogShell.layout();
			dialogShell.pack();
			dialogShell.setSize(403, 353);
			dialogShell.setText("License");

			licenseTextArea = new Text(dialogShell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			FormData licenseTextAreaLData = new FormData();
			licenseTextAreaLData.left = new FormAttachment(0, 1000, 12);
			licenseTextAreaLData.top = new FormAttachment(0, 1000, 12);
			licenseTextAreaLData.bottom = new FormAttachment(1000, 1000, -12);
			licenseTextAreaLData.right = new FormAttachment(1000, 1000, -12);
			licenseTextArea.setLayoutData(licenseTextAreaLData);
			String license = "Copyright (c) 2010, Leandro Aparecido\nAll rights reserved.\n\n";
			try {
				license += CharStreams.toString(new InputSupplier<InputStreamReader>() {
					@Override
					public InputStreamReader getInput() throws IOException {
						return new InputStreamReader(Application.class.getResourceAsStream(Application.LICENSE));
					}
				});
			} catch (IOException e) {
				logger.error("Could not load license");
			}
			licenseTextArea.setText(license);
			licenseTextArea.setBounds(12, 12, 256, 168);
			licenseTextArea.setEditable(false);

			Dialogs.centerDialog(dialogShell);
			dialogShell.open();
			Dialogs.bringToForeground(dialogShell);
		} catch (Exception e) {
			logger.error("Error showing license dialog", e);
		}
	}

}
