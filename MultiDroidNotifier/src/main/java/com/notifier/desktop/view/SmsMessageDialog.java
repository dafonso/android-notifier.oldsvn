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

import com.notifier.desktop.os.*;
import com.notifier.desktop.view.impl.*;

public class SmsMessageDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(SmsMessageDialog.class);

	private Shell dialogShell;
	private Text toText;
	private Text bodyText;
	private Button sendButton;
	private Button cancelButton;

	public SmsMessageDialog(SwtManager swtManager) {
		super(swtManager.getShell(), SWT.NULL);
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

			GridLayout shellLayout = new GridLayout();
			shellLayout.numColumns = 2;
			shellLayout.marginHeight = 10;
			shellLayout.marginWidth = 10;
			dialogShell.setLayout(shellLayout);
			dialogShell.setText("Send SMS");

			Label toLabel = new Label(dialogShell, SWT.NONE);
			toLabel.setText("To:");

			toText = new Text(dialogShell, SWT.BORDER | SWT.SINGLE);
			GridData toTextLData = new GridData();
			toTextLData.grabExcessHorizontalSpace = true;
			toTextLData.horizontalAlignment = GridData.FILL;
			toText.setLayoutData(toTextLData);

			bodyText = new Text(dialogShell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			GridData bodyTextLData = new GridData();
			bodyTextLData.grabExcessHorizontalSpace = true;
			bodyTextLData.horizontalAlignment = SWT.FILL;
			bodyTextLData.horizontalSpan = 2;
			bodyTextLData.widthHint = 400;
			bodyTextLData.heightHint = 150;
			bodyText.setLayoutData(bodyTextLData);

			Composite buttonsComposite = new Composite(dialogShell, SWT.NONE);
			GridData buttonsCompositeLData = new GridData();
			buttonsCompositeLData.horizontalSpan = 2;
			buttonsCompositeLData.horizontalAlignment = SWT.RIGHT;
			buttonsComposite.setLayoutData(buttonsCompositeLData);
			RowLayout buttonsCompositeLayout = new RowLayout();
			buttonsComposite.setLayout(buttonsCompositeLayout);

			if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.WINDOWS) {
				sendButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData sendButtonLData = new RowData(70, 25);
				sendButton.setLayoutData(sendButtonLData);
				sendButton.setText("Send");
	
				cancelButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData cancelButtonLData = new RowData(70, 25);
				cancelButton.setLayoutData(cancelButtonLData);
				cancelButton.setText("Cancel");
			} else {
				cancelButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData cancelButtonLData = new RowData(70, 25);
				cancelButton.setLayoutData(cancelButtonLData);
				cancelButton.setText("Cancel");

				sendButton = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
				RowData sendButtonLData = new RowData(70, 25);
				sendButton.setLayoutData(sendButtonLData);
				sendButton.setText("Send");
			}

			dialogShell.layout();
			dialogShell.pack();

			Dialogs.centerDialog(dialogShell);
			dialogShell.open();
			Dialogs.bringToForeground(dialogShell);
		} catch (Exception e) {
			logger.error("Error showing sms dialog", e);
		}
	}

	public static void main(String[] args) {
		SwtManager swtManager = new SwtManagerImpl();
		swtManager.start();
		SmsMessageDialog dialog = new SmsMessageDialog(swtManager);
		dialog.open();
		swtManager.runEventLoop();
	}
}
