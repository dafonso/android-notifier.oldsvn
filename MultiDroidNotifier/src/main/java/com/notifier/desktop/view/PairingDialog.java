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

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.slf4j.*;

import com.notifier.desktop.*;

public class PairingDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(PairingDialog.class);

	private Shell dialogShell;
	private Label textLabel;
	private Button cancelButton;

	public PairingDialog(SwtManager swtManager) {
		super(swtManager.getShell(), SWT.NULL);
	}

	public void open(Listener cancelListener) {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			dialogShell.setText("Pairing");
			dialogShell.setLayout(new FormLayout());
			dialogShell.addListener(SWT.Dispose, cancelListener);

			textLabel = new Label(dialogShell, SWT.NONE);
			FormData textLabelLData = new FormData();
			textLabelLData.left = new FormAttachment(0, 30);
			textLabelLData.top = new FormAttachment(0, 20);
			textLabelLData.right = new FormAttachment(100, -30);
			textLabel.setLayoutData(textLabelLData);
			textLabel.setText("Please, send a test notification from the\ndevice you wish to pair with.");
			textLabel.setAlignment(SWT.CENTER);

			cancelButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			FormData cancelButtonLData = new FormData();
			cancelButtonLData.left = new FormAttachment(30);
			cancelButtonLData.right = new FormAttachment(70);
			cancelButtonLData.top = new FormAttachment(textLabel, 40);
			cancelButtonLData.bottom = new FormAttachment(100, -10);
			cancelButtonLData.height = 30;
			cancelButton.setLayoutData(cancelButtonLData);
			cancelButton.setText("Cancel");
			cancelButton.addListener(SWT.Selection, cancelListener);

			dialogShell.layout();
			dialogShell.pack();

			Dialogs.centerDialog(dialogShell);
			dialogShell.open();
			Dialogs.bringToForeground(dialogShell);
		} catch (Exception e) {
			logger.error("Error showing pairing dialog", e);
		}
	}

	public void close() {
		dialogShell.close();
	}
}
