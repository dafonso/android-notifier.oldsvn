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
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import org.slf4j.*;

import com.notifier.desktop.*;
import com.notifier.desktop.update.*;

public class AboutDialog extends org.eclipse.swt.widgets.Dialog {

	public static final String PROJECT_URL = "http://code.google.com/p/android-notifier/";

	private static final Logger logger = LoggerFactory.getLogger(AboutDialog.class);

	private Version version;
	private SwtManager swtManager;
	private Shell dialogShell;
	private Label titleLabel;
	private Label descriptionLabel;
	private Button licenseButton;
	private Link websiteLink;
	private Button closeButton;
	private Label authorLabel;

	public AboutDialog(Version version, SwtManager swtManager) {
		super(swtManager.getShell(), SWT.NULL);
		this.version = version;
		this.swtManager = swtManager;
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM);

			swtManager.registerResourceUser(dialogShell);

			GridLayout dialogShellLayout = new GridLayout();
			dialogShellLayout.makeColumnsEqualWidth = true;
			dialogShellLayout.numColumns = 2;
			dialogShell.setLayout(dialogShellLayout);
			dialogShell.layout();
			dialogShell.pack();
			dialogShell.setSize(400, 180);
			dialogShell.setText("About " + Application.NAME);
			dialogShell.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					swtManager.setShowingAboutDialog(false);
				}
			});

			titleLabel = new Label(dialogShell, SWT.NONE);
			GridData titleLabelLData = new GridData();
			titleLabelLData.horizontalSpan = 2;
			titleLabelLData.horizontalAlignment = GridData.FILL;
			titleLabelLData.grabExcessHorizontalSpace = true;
			titleLabelLData.heightHint = 25;
			titleLabelLData.verticalIndent = 10;
			titleLabel.setLayoutData(titleLabelLData);
			titleLabel.setText("Android Notifier Desktop " + version);
			titleLabel.setFont(swtManager.getFont("Tahoma", 16, 1, false, false));
			titleLabel.setAlignment(SWT.CENTER);

			descriptionLabel = new Label(dialogShell, SWT.NONE);
			GridData descriptionLabelLData = new GridData();
			descriptionLabelLData.horizontalSpan = 2;
			descriptionLabelLData.grabExcessHorizontalSpace = true;
			descriptionLabelLData.horizontalAlignment = GridData.CENTER;
			descriptionLabelLData.verticalIndent = 5;
			descriptionLabel.setLayoutData(descriptionLabelLData);
			descriptionLabel.setText("A remote notification app for Android devices.");
			descriptionLabel.setFont(swtManager.getFont("Tahoma", 10, 0, false, false));
			descriptionLabel.setAlignment(SWT.CENTER);

			authorLabel = new Label(dialogShell, SWT.NONE);
			GridData authorLabelLData = new GridData();
			authorLabelLData.horizontalSpan = 2;
			authorLabelLData.grabExcessHorizontalSpace = true;
			authorLabelLData.horizontalAlignment = GridData.CENTER;
			authorLabelLData.verticalIndent = 3;
			authorLabel.setLayoutData(authorLabelLData);
			authorLabel.setText("Written by Leandro Aparecido");
			authorLabel.setAlignment(SWT.CENTER);

			websiteLink = new Link(dialogShell, SWT.NONE);
			GridData websiteLinkLData = new GridData();
			websiteLinkLData.verticalIndent = 2;
			websiteLinkLData.grabExcessHorizontalSpace = true;
			websiteLinkLData.horizontalSpan = 2;
			websiteLinkLData.horizontalAlignment = GridData.CENTER;
			websiteLink.setLayoutData(websiteLinkLData);
			websiteLink.setText("<a href=\"" + PROJECT_URL + "\">" + PROJECT_URL + "</a>");
			websiteLink.setFont(swtManager.getFont("Tahoma", 10, 0, false, false));
			websiteLink.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Program.launch(PROJECT_URL);
				}
			});

			licenseButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			GridData licenseButtonLData = new GridData();
			licenseButtonLData.grabExcessVerticalSpace = true;
			licenseButtonLData.grabExcessHorizontalSpace = true;
			licenseButtonLData.widthHint = 72;
			licenseButtonLData.heightHint = 27;
			licenseButtonLData.verticalIndent = 2;
			licenseButton.setLayoutData(licenseButtonLData);
			licenseButton.setText("License");
			licenseButton.setSize(72, 27);
			licenseButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					LicenseDialog dialog = new LicenseDialog(dialogShell);
					dialog.open();
				}
			});

			closeButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			GridData closeButtonLData = new GridData();
			closeButtonLData.horizontalAlignment = GridData.END;
			closeButtonLData.widthHint = 72;
			closeButtonLData.heightHint = 27;
			closeButtonLData.verticalIndent = 2;
			closeButton.setLayoutData(closeButtonLData);
			closeButton.setText("Close");
			closeButton.setFocus();
			closeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialogShell.close();
				}
			});

			Dialogs.centerDialog(dialogShell);
			dialogShell.open();
			Dialogs.bringToForeground(dialogShell);
			swtManager.setShowingAboutDialog(true);
		} catch (Exception e) {
			logger.error("Error showing about dialog", e);
		}
	}

}
