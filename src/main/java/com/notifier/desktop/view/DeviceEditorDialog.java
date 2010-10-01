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

public class DeviceEditorDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(DeviceEditorDialog.class);

	private final SwtManager swtManager;
	private final long deviceId;
	private final String suggestedDeviceName;

	private Shell dialogShell;
	private Label deviceIdLabel;
	private Text deviceNameText;
	private Button okButton;

	public DeviceEditorDialog(SwtManager swtManager, long deviceId, String suggestedDeviceName) {
		super(swtManager.getShell(), SWT.NULL);
		this.swtManager = swtManager;
		this.deviceId = deviceId;
		this.suggestedDeviceName = suggestedDeviceName;
	}

	public void open(SubmitListener listener) {
		try {
			DialogListener dialogListener = new DialogListener(listener);
			
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			dialogShell.setText("Android Device Found");

			GridLayout layout = new GridLayout();
			layout.marginHeight = 10;
			layout.marginWidth = 10;
			dialogShell.setLayout(layout);

			deviceIdLabel = new Label(dialogShell, SWT.NONE);
			GridData deviceIdLabelLData = new GridData();
			deviceIdLabel.setLayoutData(deviceIdLabelLData);
			deviceIdLabel.setText("Found device with ID " + deviceId + ", please enter a name for it:");
			deviceIdLabel.setAlignment(SWT.CENTER);

			deviceNameText = new Text(dialogShell, SWT.BORDER | SWT.SINGLE);
			GridData deviceNameTextLData = new GridData();
			deviceNameTextLData.grabExcessHorizontalSpace = true;
			deviceNameTextLData.horizontalAlignment = GridData.FILL;
			deviceNameText.setLayoutData(deviceNameTextLData);
			deviceNameText.setText("HTC Nexus One");
			deviceNameText.addListener(SWT.DefaultSelection, dialogListener);
			// Set text in different thread so the dialog will not depend on the size of the text
			swtManager.update(new Runnable() {
				@Override
				public void run() {
					deviceNameText.setText(suggestedDeviceName);
					deviceNameText.selectAll();
				}
			});

			okButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			GridData okButtonLData = new GridData(70, 25);
			okButtonLData.horizontalAlignment = SWT.CENTER;
			okButton.setLayoutData(okButtonLData);
			okButton.setText("OK");
			okButton.addListener(SWT.Selection, dialogListener);

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

	public static interface SubmitListener {
		boolean onDeviceName(String name);
	}

	class DialogListener implements Listener {

		private final SubmitListener listener;

		public DialogListener(SubmitListener listener) {
			this.listener = listener;
		}

		@Override
		public void handleEvent(Event event) {
			String name = deviceNameText.getText();
			if (!listener.onDeviceName(name)) {
				Dialogs.showError(swtManager, "Name already in use", "This name is already being used for another device.", false);
			}
		}
	}

}
