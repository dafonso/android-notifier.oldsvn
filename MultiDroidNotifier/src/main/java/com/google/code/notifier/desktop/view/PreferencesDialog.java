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

import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.slf4j.*;

import com.google.code.notifier.desktop.*;
import com.google.code.notifier.desktop.app.*;
import com.google.code.notifier.desktop.app.OperatingSystems.*;
import com.google.code.notifier.desktop.util.*;
import com.google.common.base.*;

public class PreferencesDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(PreferencesDialog.class);

	private static final String PREF_CHANGE_THREAD_NAME = "preferences";

	private final Application application;
	private final ApplicationPreferences preferences;
	private final NotificationManager notificationManager;
	private final SwtManager swtManager;

	private Shell dialogShell;

	private Group generalGroup;
	private Button startAtLoginCheckbox;

	private Group notificationReceptionMethodsGroup;
	private Button wifiCheckbox;
	private Button internetCheckbox;
	private Button bluetoothCheckbox;
	// private Button usbCheckbox; USB notifications are not yet supported by the android app

	private Group notificationDisplayMethodsGroup;
	private Button systemDefaultCheckbox;
	private Button growlCheckbox;
	private Button libnotifyCheckbox;

	private Group devicesGroup;
	private Button anyDeviceRadioButton;
	private Button onlyDeviceRadioButton;
	private List devicesList;
	private Button addDeviceButton;
	private Button removeDeviceButton;

	private Group notificationActionsGroup;
	private TabFolder notificationTypesTabFolder;

	private Button okButton;

	public PreferencesDialog(Application application, NotificationManager notificationManager, SwtManager swtManager) {
		super(swtManager.getShell(), SWT.NULL);
		this.application = application;
		this.notificationManager = notificationManager;
		this.swtManager = swtManager;
		preferences = new ApplicationPreferences();
	}

	public void open() {
		try {
			preferences.read();

			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM);

			dialogShell.setLayout(new FormLayout());
			dialogShell.setText(Application.NAME + " Preferences");
			dialogShell.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					try {
						preferences.write();
					} catch (IOException ex) {
						logger.error("Error saving preferences", ex);
						Dialogs.showError(swtManager, "Error saving preferences", "An error ocurred while saving preferences. Please, try again.", false);
					} finally {
						swtManager.setShowingPreferencesDialog(false);
					}
				}
			});

			// General Group

			generalGroup = new Group(dialogShell, SWT.NONE);
			GridLayout generalGroupLayout = new GridLayout();
			generalGroupLayout.makeColumnsEqualWidth = true;
			generalGroup.setLayout(generalGroupLayout);
			FormData generalGroupLData = new FormData();
			generalGroupLData.top = new FormAttachment(0, 10);
			generalGroupLData.left = new FormAttachment(0, 10);
			generalGroupLData.right = new FormAttachment(100, -10);
			generalGroup.setLayoutData(generalGroupLData);
			generalGroup.setText("General Options");

			startAtLoginCheckbox = new Button(generalGroup, SWT.CHECK | SWT.LEFT);
			GridData startAtLoginCheckboxLData = new GridData();
			startAtLoginCheckboxLData.horizontalIndent = 5;
			startAtLoginCheckbox.setLayoutData(startAtLoginCheckboxLData);
			startAtLoginCheckbox.setText("Start at login");
			startAtLoginCheckbox.setToolTipText("Start Android Notifier Desktop when you login (Windows-only)");
			startAtLoginCheckbox.setSelection(preferences.isStartAtLogin());
			startAtLoginCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (OperatingSystems.CURRENT_FAMILY != Family.WINDOWS) {
						Dialogs.showError(swtManager, "Start at Login Support", "Please, use your system Startup Manager to start me at login.", false);
						startAtLoginCheckbox.setSelection(false);
					} else {
						final boolean enabled = startAtLoginCheckbox.getSelection();
						new Thread(new Runnable() {
							@Override
							public void run() {
								if (application.adjustStartAtLogin(enabled, false)) {
									preferences.setStartAtLogin(enabled);
								} else {
									preferences.setStartAtLogin(!enabled);
									swtManager.update(new Runnable() {
										@Override
										public void run() {
											if (!startAtLoginCheckbox.isDisposed()) {
												startAtLoginCheckbox.setSelection(!enabled);
											}
										}
									});
								}
							}
						}, PREF_CHANGE_THREAD_NAME).start();
					}
				}
			});

			// Notification Reception Group

			notificationReceptionMethodsGroup = new Group(dialogShell, SWT.NONE);
			GridLayout notificationMethodsGroupLayout = new GridLayout();
			notificationReceptionMethodsGroup.setLayout(notificationMethodsGroupLayout);
			FormData notificationMethodsGroupLData = new FormData();
			notificationMethodsGroupLData.top = new FormAttachment(generalGroup, 5);
			notificationMethodsGroupLData.left = new FormAttachment(0, 10);
			notificationMethodsGroupLData.right = new FormAttachment(100, -10);
			notificationReceptionMethodsGroup.setLayoutData(notificationMethodsGroupLData);
			notificationReceptionMethodsGroup.setText("Notification Reception Methods");

			wifiCheckbox = new Button(notificationReceptionMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData wifiCheckboxLData = new GridData();
			wifiCheckboxLData.horizontalIndent = 5;
			wifiCheckbox.setLayoutData(wifiCheckboxLData);
			
			// This description is for information and help configuring the android app
			// This app will bind to all available network interfaces to maximize chances of success
			String hostDescription;
			String localHostName = InetAddresses.getLocalHostName();
			String localHostAddress = InetAddresses.getLocalHostAddress();
			if (localHostName == null && localHostAddress == null) {
				hostDescription = "";
			} else {
				hostDescription = "(";
				hostDescription += Strings.isNullOrEmpty(localHostName) ? "" : localHostName + " / ";
				hostDescription += localHostAddress;
				hostDescription += ")";
			}
			wifiCheckbox.setText("Wifi " + hostDescription);
			wifiCheckbox.setSelection(preferences.isReceptionWithWifi());
			wifiCheckbox.setToolTipText("Use this address to setup your device to send notifications on local network or setup your router to forward connections incoming over cell network. Make sure port 10600 is open in firewall, see Setup wiki page for more information");
			wifiCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = wifiCheckbox.getSelection();
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (!enabled && preferences.isReceptionWithUpnp()) {
								application.adjustUpnpReceiver(false);
								preferences.setReceptionWithUpnp(false);
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										if (!internetCheckbox.isDisposed()) {
											internetCheckbox.setSelection(false);
										}
									}
								});
							}
							if (application.adjustWifiReceiver(enabled)) {
								preferences.setReceptionWithWifi(enabled);
							} else {
								preferences.setReceptionWithWifi(!enabled);
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										if (!wifiCheckbox.isDisposed()) {
											wifiCheckbox.setSelection(!enabled);
										}
									}
								});
							}
						}
					}, PREF_CHANGE_THREAD_NAME).start();
				}
			});

			internetCheckbox = new Button(notificationReceptionMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData internetCheckboxLData = new GridData();
			internetCheckboxLData.horizontalIndent = 5;
			internetCheckbox.setLayoutData(internetCheckboxLData);
			internetCheckbox.setText("Internet");
			internetCheckbox.setSelection(preferences.isReceptionWithUpnp());
			internetCheckbox.setToolTipText("Make " + Application.NAME + " configure port forwarding automatically in your router to get notifications over cell network, see Setup wiki page for more information");
			internetCheckbox.setEnabled(preferences.isReceptionWithWifi());
			internetCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = internetCheckbox.getSelection();
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (application.adjustUpnpReceiver(enabled)) {
								preferences.setReceptionWithUpnp(enabled);
							} else {
								preferences.setReceptionWithUpnp(!enabled);
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										if (!internetCheckbox.isDisposed()) {
											internetCheckbox.setSelection(!enabled);
										}
									}
								});
							}
						}
					}, PREF_CHANGE_THREAD_NAME).start();
				}
			});
			wifiCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					internetCheckbox.setEnabled(wifiCheckbox.getSelection());
				}
			});

			bluetoothCheckbox = new Button(notificationReceptionMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData bluetoothCheckboxLData = new GridData();
			bluetoothCheckboxLData.horizontalIndent = 5;
			bluetoothCheckbox.setLayoutData(bluetoothCheckboxLData);
			bluetoothCheckbox.setText("Bluetooth");
			bluetoothCheckbox.setSelection(preferences.isReceptionWithBluetooth());
			bluetoothCheckbox.setToolTipText("See how-to on the project wiki to setup bluetooth connections");
			bluetoothCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = bluetoothCheckbox.getSelection();
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (application.adjustBluetoothReceiver(enabled)) {
								preferences.setReceptionWithBluetooth(enabled);
							} else {
								preferences.setReceptionWithBluetooth(!enabled);
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										if (!bluetoothCheckbox.isDisposed()) {
											bluetoothCheckbox.setSelection(!enabled);
										}
									}
								});
							}
						}
					}, PREF_CHANGE_THREAD_NAME).start();
				}
			});

			/*
			 * usbCheckbox = new Button(notificationReceptionMethodsGroup, SWT.CHECK | SWT.LEFT);
			 * GridData usbCheckboxLData = new GridData();
			 * usbCheckboxLData.horizontalIndent = 5;
			 * usbCheckbox.setLayoutData(usbCheckboxLData);
			 * usbCheckbox.setText("USB");
			 * usbCheckbox.setSelection(preferences.isReceptionWithUsb());
			 * usbCheckbox.setEnabled(false);
			 * usbCheckbox.addListener(SWT.Selection, new Listener() {
			 * 
			 * @Override
			 * public void handleEvent(Event event) {
			 * preferences.setReceptionWithUsb(usbCheckbox.getSelection());
			 * }
			 * });
			 */
			// Notification Display Group

			notificationDisplayMethodsGroup = new Group(dialogShell, SWT.NONE);
			GridLayout notificationDisplayMethodsGroupLayout = new GridLayout();
			notificationDisplayMethodsGroupLayout.makeColumnsEqualWidth = true;
			notificationDisplayMethodsGroup.setLayout(notificationDisplayMethodsGroupLayout);
			FormData notificationDisplayMethodsGroupLData = new FormData();
			notificationDisplayMethodsGroupLData.top = new FormAttachment(notificationReceptionMethodsGroup, 5);
			notificationDisplayMethodsGroupLData.left = new FormAttachment(0, 10);
			notificationDisplayMethodsGroupLData.right = new FormAttachment(100, -10);
			notificationDisplayMethodsGroup.setLayoutData(notificationDisplayMethodsGroupLData);
			notificationDisplayMethodsGroup.setText("Notification Display Methods");

			systemDefaultCheckbox = new Button(notificationDisplayMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData systemDefaultCheckboxLData = new GridData();
			systemDefaultCheckboxLData.horizontalIndent = 5;
			systemDefaultCheckbox.setLayoutData(systemDefaultCheckboxLData);
			systemDefaultCheckbox.setText("System default");
			systemDefaultCheckbox.setToolTipText("Use system default mechanism to show notifications");
			systemDefaultCheckbox.setSelection(preferences.isDisplayWithSystemDefault());
			systemDefaultCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = systemDefaultCheckbox.getSelection();
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (application.adjustSystemDefaultBroadcaster(enabled)) {
								preferences.setDisplayWithSystemDefault(enabled);
							} else {
								preferences.setDisplayWithSystemDefault(!enabled);
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										if (!systemDefaultCheckbox.isDisposed()) {
											systemDefaultCheckbox.setSelection(!enabled);
										}
									}
								});
							}
						}
					}, PREF_CHANGE_THREAD_NAME).start();
				}
			});

			growlCheckbox = new Button(notificationDisplayMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData growlCheckboxLData = new GridData();
			growlCheckboxLData.horizontalIndent = 5;
			growlCheckbox.setLayoutData(growlCheckboxLData);
			growlCheckbox.setText("Growl Notification Transport Protocol");
			growlCheckbox.setSelection(preferences.isDisplayWithGrowl());
			growlCheckbox.setToolTipText("Use GNTP to show notifications, network notifications must be enabled in Growl, see how-to on the project wiki to setup");
			growlCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = growlCheckbox.getSelection();
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (application.adjustGrowlBroadcaster(enabled)) {
								preferences.setDisplayWithGrowl(enabled);
							} else {
								preferences.setDisplayWithGrowl(!enabled);
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										if (!growlCheckbox.isDisposed()) {
											growlCheckbox.setSelection(!enabled);
										}
									}
								});
							}
						}
					}, PREF_CHANGE_THREAD_NAME).start();
				}
			});

			if (OperatingSystems.CURRENT_FAMILY == Family.LINUX) {
				libnotifyCheckbox = new Button(notificationDisplayMethodsGroup, SWT.CHECK | SWT.LEFT);
				GridData libnotifyCheckboxLData = new GridData();
				libnotifyCheckboxLData.horizontalIndent = 5;
				libnotifyCheckbox.setLayoutData(libnotifyCheckboxLData);
				libnotifyCheckbox.setText("Libnotify");
				libnotifyCheckbox.setToolTipText("Use notify-send command to show notifications");
				libnotifyCheckbox.setSelection(preferences.isDisplayWithLibnotify());
				libnotifyCheckbox.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						final boolean enabled = libnotifyCheckbox.getSelection();
						new Thread(new Runnable() {
							@Override
							public void run() {
								if (application.adjustLibnotifyBroadcaster(enabled)) {
									preferences.setDisplayWithLibnotify(enabled);
								} else {
									preferences.setDisplayWithLibnotify(!enabled);
									swtManager.update(new Runnable() {
										@Override
										public void run() {
											if (!libnotifyCheckbox.isDisposed()) {
												libnotifyCheckbox.setSelection(!enabled);
											}
										}
									});
								}
							}
						}, PREF_CHANGE_THREAD_NAME).start();
					}
				});
			}

			// Notification Actions Groups
			notificationActionsGroup = new Group(dialogShell, SWT.NONE);
			FillLayout notificationActionsGroupLayout = new FillLayout();
			notificationActionsGroupLayout.marginHeight = 5;
			notificationActionsGroup.setLayout(notificationActionsGroupLayout);
			FormData notificationActionsGroupLData = new FormData();
			notificationActionsGroupLData.top = new FormAttachment(notificationDisplayMethodsGroup, 5);
			notificationActionsGroupLData.left = new FormAttachment(0, 10);
			notificationActionsGroupLData.right = new FormAttachment(100, -10);
			notificationActionsGroup.setLayoutData(notificationActionsGroupLData);
			notificationActionsGroup.setText("Notification Actions");

			notificationTypesTabFolder = new TabFolder(notificationActionsGroup, SWT.NONE);
			for (Notification.Type type : Notification.Type.values()) {
				createNotificationTypeTabItem(notificationTypesTabFolder, type);
			}
			notificationTypesTabFolder.pack();

			// Devices Group

			devicesGroup = new Group(dialogShell, SWT.NONE);
			GridLayout devicesGroupLayout = new GridLayout();
			devicesGroupLayout.numColumns = 2;
			devicesGroup.setLayout(devicesGroupLayout);
			FormData devicesGroupLData = new FormData();
			devicesGroupLData.top = new FormAttachment(notificationActionsGroup, 5);
			devicesGroupLData.left = new FormAttachment(0, 10);
			devicesGroupLData.right = new FormAttachment(100, -10);
			devicesGroup.setLayoutData(devicesGroupLData);
			devicesGroup.setText("Devices");

			anyDeviceRadioButton = new Button(devicesGroup, SWT.RADIO | SWT.LEFT);
			GridData anyDeviceComboBoxLData = new GridData();
			anyDeviceComboBoxLData.horizontalIndent = 5;
			anyDeviceComboBoxLData.horizontalSpan = 2;
			anyDeviceRadioButton.setLayoutData(anyDeviceComboBoxLData);
			anyDeviceRadioButton.setText("Receive notifications from any device");
			anyDeviceRadioButton.setSelection(preferences.isReceptionFromAnyDevice());
			anyDeviceRadioButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					notificationManager.setReceptionFromAnyDevice(anyDeviceRadioButton.getSelection());
					preferences.setReceptionFromAnyDevice(anyDeviceRadioButton.getSelection());
					devicesList.setEnabled(false);
					addDeviceButton.setEnabled(false);
					removeDeviceButton.setEnabled(false);
				}
			});

			onlyDeviceRadioButton = new Button(devicesGroup, SWT.RADIO | SWT.LEFT);
			GridData onlyDeviceRadioButtonLData = new GridData();
			onlyDeviceRadioButtonLData.horizontalIndent = 5;
			onlyDeviceRadioButtonLData.horizontalSpan = 2;
			onlyDeviceRadioButton.setLayoutData(onlyDeviceRadioButtonLData);
			onlyDeviceRadioButton.setText("Receive notifications only from these devices");
			onlyDeviceRadioButton.setSelection(!preferences.isReceptionFromAnyDevice());
			onlyDeviceRadioButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					notificationManager.setReceptionFromAnyDevice(!onlyDeviceRadioButton.getSelection());
					preferences.setReceptionFromAnyDevice(!onlyDeviceRadioButton.getSelection());
					devicesList.setEnabled(true);
					addDeviceButton.setEnabled(true);
					removeDeviceButton.setEnabled(true);
				}
			});

			GridData devicesListLData = new GridData();
			devicesListLData.heightHint = 63;
			devicesListLData.verticalSpan = 2;
			devicesListLData.grabExcessHorizontalSpace = true;
			devicesListLData.horizontalAlignment = GridData.FILL;
			devicesList = new List(devicesGroup, SWT.SINGLE | SWT.BORDER);
			devicesList.setLayoutData(devicesListLData);
			devicesList.setEnabled(!preferences.isReceptionFromAnyDevice());
			for (String deviceId : preferences.getAllowedDevicesIds()) {
				devicesList.add(deviceId);
			}

			addDeviceButton = new Button(devicesGroup, SWT.PUSH | SWT.CENTER);
			GridData addDeviceButtonLData = new GridData();
			addDeviceButtonLData.widthHint = 76;
			addDeviceButtonLData.heightHint = 29;
			addDeviceButton.setLayoutData(addDeviceButtonLData);
			addDeviceButton.setText("Add");
			addDeviceButton.setEnabled(!preferences.isReceptionFromAnyDevice());
			addDeviceButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final PairingDialog dialog = new PairingDialog(swtManager);
					dialog.open(new Listener() {
						@Override
						public void handleEvent(Event cancelEvent) {
							notificationManager.cancelWaitForPairing();
							dialog.close();
						}
					});
					notificationManager.waitForPairing(new NotificationManager.PairingListener() {
						@Override
						public boolean onPairingSuccessful(final String deviceId) {
							if (preferences.addAllowedDeviceId(deviceId)) {
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										devicesList.add(deviceId);
										notificationManager.setPairedDevices(devicesList.getItems());
										dialog.close();
									}
								});
								return true;
							}
							return false;
						}
					});
				}
			});

			removeDeviceButton = new Button(devicesGroup, SWT.PUSH | SWT.CENTER);
			GridData removeDeviceButtonLData = new GridData();
			removeDeviceButtonLData.widthHint = 76;
			removeDeviceButtonLData.heightHint = 29;
			removeDeviceButton.setLayoutData(removeDeviceButtonLData);
			removeDeviceButton.setText("Remove");
			removeDeviceButton.setEnabled(!preferences.isReceptionFromAnyDevice());
			removeDeviceButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					String[] items = devicesList.getSelection();
					if (items.length > 0) {
						String deviceId = items[0];
						preferences.removeAllowedDeviceId(deviceId);
						devicesList.remove(deviceId);
						notificationManager.setPairedDevices(devicesList.getItems());
					}
				}
			});

			// OK/Cancel Buttons

			okButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			FormData okButtonLData = new FormData();
			okButtonLData.width = 70;
			okButtonLData.height = 28;
			okButtonLData.top = new FormAttachment(devicesGroup, 10);
			okButtonLData.right = new FormAttachment(100, -10);
			okButtonLData.bottom = new FormAttachment(100, -10);
			okButton.setLayoutData(okButtonLData);
			okButton.setText("Save");
			okButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					dialogShell.close();
				}
			});
/* No cancel support
			cancelButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			FormData cancelButtonLData = new FormData();
			cancelButtonLData.width = 70;
			cancelButtonLData.height = 28;
			cancelButtonLData.top = new FormAttachment(devicesGroup, 10);
			cancelButtonLData.right = new FormAttachment(okButton, -10);
			cancelButtonLData.bottom = new FormAttachment(100, -10);
			cancelButton.setLayoutData(cancelButtonLData);
			cancelButton.setText("Cancel");
			cancelButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					dialogShell.close();
				}
			});
*/
			dialogShell.layout();
			dialogShell.pack();

			Dialogs.centerDialog(dialogShell);
			dialogShell.open();
			Dialogs.bringToForeground(dialogShell);
			swtManager.setShowingPreferencesDialog(true);
		} catch (Exception e) {
			logger.error("Error showing preferences dialog", e);
		}
	}

	protected TabItem createNotificationTypeTabItem(TabFolder parent, final Notification.Type type) {
		TabItem item = new TabItem(parent, SWT.NONE);
		item.setText(type.toString());

		Composite composite = new Composite(item.getParent(), SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		final Button enabledCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		GridData enabledCheckboxLData = new GridData();
		enabledCheckboxLData.horizontalIndent = 5;
		enabledCheckboxLData.horizontalSpan = 3;
		enabledCheckbox.setLayoutData(enabledCheckboxLData);
		enabledCheckbox.setText("Enabled");
		enabledCheckbox.setSelection(preferences.isNotificationEnabled(type));
		enabledCheckbox.setToolTipText("Enable/Disable this notification completely");
		enabledCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final boolean enabled = enabledCheckbox.getSelection();
				preferences.setNotificationEnabled(type, enabled);
				notificationManager.setNotificationEnabled(type, enabled);
			}
		});

		final Button clipboardCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		GridData clipboardCheckboxLData = new GridData();
		clipboardCheckboxLData.horizontalIndent = 5;
		clipboardCheckboxLData.horizontalSpan = 3;
		clipboardCheckbox.setLayoutData(clipboardCheckboxLData);
		clipboardCheckbox.setText("Copy to clipboard");
		clipboardCheckbox.setSelection(preferences.isNotificationClipboard(type));
		clipboardCheckbox.setToolTipText("Copy notification description to clipboard when it arrives");
		clipboardCheckbox.setEnabled(preferences.isNotificationEnabled(type));
		clipboardCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final boolean enabled = clipboardCheckbox.getSelection();
				preferences.setNotificationClipboard(type, enabled);
				notificationManager.setNotificationClipboard(type, enabled);
			}
		});
		enabledCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				clipboardCheckbox.setEnabled(enabledCheckbox.getSelection());
			}
		});

		final Button executeCommandCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		GridData executeCommandCheckboxLData = new GridData();
		executeCommandCheckboxLData.horizontalIndent = 5;
		executeCommandCheckboxLData.horizontalSpan = 3;
		executeCommandCheckbox.setLayoutData(executeCommandCheckboxLData);
		executeCommandCheckbox.setText("Execute Commands");
		executeCommandCheckbox.setSelection(preferences.isNotificationExecuteCommand(type));
		executeCommandCheckbox.setToolTipText("Execute commands specified below when notification arrives");
		executeCommandCheckbox.setEnabled(preferences.isNotificationEnabled(type));
		executeCommandCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final boolean enabled = executeCommandCheckbox.getSelection();
				preferences.setNotificationExecuteCommand(type, enabled);
				notificationManager.setNotificationExecuteCommand(type, enabled);
			}
		});
		enabledCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				executeCommandCheckbox.setEnabled(enabledCheckbox.getSelection());
			}
		});

		Label commandLabel = new Label(composite, SWT.NONE);
		GridData commandLabelLData = new GridData();
		commandLabel.setLayoutData(commandLabelLData);
		commandLabel.setText("Commands:");

		final Text commandText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		GridData commandTextLData = new GridData();
	    commandTextLData.grabExcessHorizontalSpace = true;
	    commandTextLData.horizontalAlignment = GridData.FILL;

		commandText.setLayoutData(commandTextLData);
		// Set text in different thread so the dialog will not depend on the size of the text
		swtManager.update(new Runnable() {
			@Override
			public void run() {
				commandText.setText(preferences.getNotificationCommand(type));
			}
		});
		commandText.setMessage("Add commands separated by ;");
		commandText.setToolTipText("Commands are not validated, if they contain spaces, put them in double quotes (do not put parameters in double quotes)");
		if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.LINUX) { // Disabled text fields in linux don't look good
			commandText.setEditable(preferences.isNotificationEnabled(type) && preferences.isNotificationExecuteCommand(type));
		} else {
			commandText.setEnabled(preferences.isNotificationEnabled(type) && preferences.isNotificationExecuteCommand(type));
		}
		commandText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String command = commandText.getText();
				preferences.setNotificationCommand(type, command);
				notificationManager.setNotificationCommand(type, command);
			}
		});

		final Button selectFileButton = new Button(composite, SWT.PUSH | SWT.CENTER);
		selectFileButton.setText("Add...");
		selectFileButton.setEnabled(preferences.isNotificationEnabled(type) && preferences.isNotificationExecuteCommand(type));
		selectFileButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(dialogShell, SWT.OPEN);
				dialog.setText("Choose command");
				dialog.setFilterExtensions(OperatingSystems.getExecutableFileExtensions());
				dialog.setFilterNames(OperatingSystems.getExecutableFileExtensionsNames());
				dialog.setFilterPath(OperatingSystems.getApplicationsRoot());
				String file = dialog.open();
				if (file != null) {
					String executable = OperatingSystems.getExecutable(file);
					String command = commandText.getText().trim();
					if (!command.isEmpty() && !command.trim().endsWith(";")) {
						executable = "; " + executable;
					}
					commandText.append(executable);
				}
			}
		});
		executeCommandCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				boolean enabled = executeCommandCheckbox.getSelection();
				if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.LINUX) {
					commandText.setEditable(enabled);
				} else {
					commandText.setEnabled(enabled);
				}
				selectFileButton.setEnabled(enabled);
			}
		});
		enabledCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				boolean enabled = enabledCheckbox.getSelection() && executeCommandCheckbox.getSelection();
				if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.LINUX) {
					commandText.setEditable(enabled);
				} else {
					commandText.setEnabled(enabled);
				}
				selectFileButton.setEnabled(enabled);
			}
		});

		item.setControl(composite);

		return item;
	}
}
