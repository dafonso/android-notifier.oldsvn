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
import java.util.concurrent.*;

import org.eclipse.nebula.widgets.pgroup.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.util.concurrent.*;
import com.notifier.desktop.*;
import com.notifier.desktop.ApplicationPreferences.Group;
import com.notifier.desktop.app.*;
import com.notifier.desktop.app.OperatingSystems.*;
import com.notifier.desktop.util.*;

public class PreferencesDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(PreferencesDialog.class);

	private static final String PASSWORD_SET_MESSAGE = "Passphrase is set, click here to change it";
	private static final String PASSWORD_NOT_SET_MESSAGE = "Passphrase is not set, click here to set one";

	private final Application application;
	private final ApplicationPreferences preferences;
	private final NotificationManager notificationManager;
	private final NotificationParser<?> notificationParser;
	private final SwtManager swtManager;
	private final ExecutorService executorService;

	private Shell dialogShell;

	private PGroup generalGroup;
	private Button startAtLoginCheckbox;
	private Button privateModeCheckbox;

	private PGroup notificationReceptionMethodsGroup;
	private Button wifiCheckbox;
	private Button internetCheckbox;
	private Button bluetoothCheckbox;
	// private Button usbCheckbox; USB notifications are not yet supported by the android app

	private Button encryptCommunicationCheckbox;
	private Text communicationPasswordText;
	private boolean communicationPasswordChanged;

	private PGroup notificationDisplayMethodsGroup;
	private Button systemDefaultCheckbox;
	private Button growlCheckbox;
	private Button libnotifyCheckbox;
	private Button msnCheckbox;
	private Button msnDetailsCheckbox;

	private PGroup devicesGroup;
	private Button anyDeviceRadioButton;
	private Button onlyDeviceRadioButton;
	private List devicesList;
	private Button addDeviceButton;
	private Button removeDeviceButton;

	private PGroup notificationActionsGroup;
	private TabFolder notificationTypesTabFolder;

	private Button okButton;

	private BiMap<Long, String> pairedDevices;

	public PreferencesDialog(Application application, NotificationManager notificationManager, NotificationParser<?> notificationParser, SwtManager swtManager, ExecutorService executorService) {
		super(swtManager.getShell(), SWT.NULL);
		this.application = application;
		this.notificationManager = notificationManager;
		this.notificationParser = notificationParser;
		this.swtManager = swtManager;
		this.executorService = executorService;
		preferences = new ApplicationPreferences();
		pairedDevices = HashBiMap.create();
	}

	public void open() {
		try {
			preferences.read();

			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM);
			dialogShell.setBackgroundMode(SWT.INHERIT_DEFAULT);

			FormLayout shellLayout = new FormLayout();
			shellLayout.marginBottom = 10;
			dialogShell.setLayout(shellLayout);
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

			generalGroup = createPGroup();
			GridLayout generalGroupLayout = new GridLayout();
			generalGroupLayout.makeColumnsEqualWidth = true;
			generalGroup.setLayout(generalGroupLayout);
			FormData generalGroupLData = new FormData();
			generalGroupLData.top = new FormAttachment(0, 7);
			generalGroupLData.left = new FormAttachment(0, 10);
			generalGroupLData.right = new FormAttachment(100, -10);
			generalGroup.setLayoutData(generalGroupLData);
			generalGroup.setText("General Options");
			generalGroup.addExpandListener(new GroupExpandListener(ApplicationPreferences.Group.GENERAL));

			startAtLoginCheckbox = new Button(generalGroup, SWT.CHECK | SWT.LEFT);
			GridData startAtLoginCheckboxLData = new GridData();
			startAtLoginCheckboxLData.horizontalIndent = 5;
			startAtLoginCheckbox.setLayoutData(startAtLoginCheckboxLData);
			startAtLoginCheckbox.setText("Start at login");
			startAtLoginCheckbox.setToolTipText("Start Android Notifier Desktop when you login");
			startAtLoginCheckbox.setSelection(preferences.isStartAtLogin());
			startAtLoginCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (OperatingSystems.CURRENT_FAMILY == Family.MAC) {
						Dialogs.showError(swtManager, "Start at Login Support", "Please, use your system Startup Manager to start me at login.", false);
						startAtLoginCheckbox.setSelection(false);
					} else {
						final boolean enabled = startAtLoginCheckbox.getSelection();
						executorService.execute(new Runnable() {
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
						});
					}
				}
			});

			privateModeCheckbox = new Button(generalGroup, SWT.CHECK | SWT.LEFT);
			GridData privateModeCheckboxLData = new GridData();
			privateModeCheckboxLData.horizontalIndent = 5;
			privateModeCheckbox.setLayoutData(privateModeCheckboxLData);
			privateModeCheckbox.setText("Private mode");
			privateModeCheckbox.setToolTipText("Notifications will not show contact details, for example, names and phone numbers");
			privateModeCheckbox.setSelection(preferences.isPrivateMode());
			privateModeCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					boolean enabled = privateModeCheckbox.getSelection();
					notificationManager.setPrivateMode(enabled);
					preferences.setPrivateMode(enabled);
				}
			});
			// group.setExpanded() must be called after initialization of the group contents
			// so nested widgets are correctly hidden if group is not expanded
			generalGroup.setExpanded(preferences.isGroupExpanded(ApplicationPreferences.Group.GENERAL));

			// Notification Reception Group

			notificationReceptionMethodsGroup = createPGroup();
			GridLayout notificationMethodsGroupLayout = new GridLayout();
			notificationMethodsGroupLayout.numColumns = 2;
			notificationReceptionMethodsGroup.setLayout(notificationMethodsGroupLayout);
			FormData notificationMethodsGroupLData = new FormData();
			notificationMethodsGroupLData.top = new FormAttachment(generalGroup, 2);
			notificationMethodsGroupLData.left = new FormAttachment(0, 10);
			notificationMethodsGroupLData.right = new FormAttachment(100, -10);
			notificationReceptionMethodsGroup.setLayoutData(notificationMethodsGroupLData);
			notificationReceptionMethodsGroup.setText("Notification Reception Methods");
			notificationReceptionMethodsGroup.addExpandListener(new GroupExpandListener(ApplicationPreferences.Group.RECEPTION));

			wifiCheckbox = new Button(notificationReceptionMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData wifiCheckboxLData = new GridData();
			wifiCheckboxLData.horizontalIndent = 5;
			wifiCheckboxLData.horizontalSpan = 2;
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
					Future<Service.State> future = application.adjustWifiReceiver(enabled);
					new ServiceAdjustListener(future, wifiCheckbox, new PreferenceSetter() {
						@Override
						public void onSuccess() {
							preferences.setReceptionWithWifi(enabled);
						}

						@Override
						public void onError() {
							preferences.setReceptionWithWifi(false);
						}
					}).start();
				}
			});

			internetCheckbox = new Button(notificationReceptionMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData internetCheckboxLData = new GridData();
			internetCheckboxLData.horizontalIndent = 5;
			internetCheckboxLData.horizontalSpan = 2;
			internetCheckbox.setLayoutData(internetCheckboxLData);
			internetCheckbox.setText("Internet (UPnP)");
			internetCheckbox.setSelection(preferences.isReceptionWithUpnp());
			internetCheckbox.setToolTipText("Make " + Application.NAME + " configure port forwarding automatically in your router to get notifications over cell network, see Setup wiki page for more information");
			internetCheckbox.setEnabled(preferences.isReceptionWithWifi());
			internetCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = internetCheckbox.getSelection();
					Future<Service.State> future = application.adjustUpnpReceiver(enabled);
					new ServiceAdjustListener(future, internetCheckbox, new PreferenceSetter() {
						@Override
						public void onSuccess() {
							preferences.setReceptionWithUpnp(enabled);
						}

						@Override
						public void onError() {
							preferences.setReceptionWithUpnp(false);
						}
					}).start();
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
			bluetoothCheckboxLData.horizontalSpan = 2;
			bluetoothCheckbox.setLayoutData(bluetoothCheckboxLData);
			bluetoothCheckbox.setText("Bluetooth");
			bluetoothCheckbox.setSelection(preferences.isReceptionWithBluetooth());
			bluetoothCheckbox.setToolTipText("See how-to on the project wiki to setup bluetooth connections");
			bluetoothCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = bluetoothCheckbox.getSelection();
					Future<Service.State> future = application.adjustBluetoothReceiver(enabled);
					new ServiceAdjustListener(future, bluetoothCheckbox, new PreferenceSetter() {
						@Override
						public void onSuccess() {
							preferences.setReceptionWithBluetooth(enabled);
						}

						@Override
						public void onError() {
							preferences.setReceptionWithBluetooth(false);
						}
					}).start();
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

			encryptCommunicationCheckbox = new Button(notificationReceptionMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData encryptCommunicationCheckboxLData = new GridData();
			encryptCommunicationCheckboxLData.horizontalIndent = 5;
			encryptCommunicationCheckboxLData.horizontalSpan = 2;
			encryptCommunicationCheckbox.setLayoutData(encryptCommunicationCheckboxLData);
			encryptCommunicationCheckbox.setText("Decrypt notifications");
			encryptCommunicationCheckbox.setSelection(preferences.isEncryptCommunication());
			encryptCommunicationCheckbox.setToolTipText("'Encrypt notifications' must be enabled in android app");
			encryptCommunicationCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					boolean enabled = encryptCommunicationCheckbox.getSelection();
					byte[] key = new byte[0];
					notificationParser.setEncryption(enabled, key);
					preferences.setEncryptCommunication(enabled);
					preferences.setCommunicationPassword(key);
					communicationPasswordText.setMessage(PASSWORD_NOT_SET_MESSAGE);
				}
			});

			Label communicationPasswordLabel = new Label(notificationReceptionMethodsGroup, SWT.NONE);
			GridData communicationPasswordLabelLData = new GridData();
			communicationPasswordLabelLData.horizontalIndent = 5;
			communicationPasswordLabel.setLayoutData(communicationPasswordLabelLData);
			communicationPasswordLabel.setText("Passphrase:");

			communicationPasswordText = new Text(notificationReceptionMethodsGroup, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
			GridData communicationPasswordTextLData = new GridData();
			communicationPasswordTextLData.grabExcessHorizontalSpace = true;
			communicationPasswordTextLData.horizontalAlignment = GridData.FILL;
			communicationPasswordText.setLayoutData(communicationPasswordTextLData);
			if (preferences.getCommunicationPassword().length > 0) {
				communicationPasswordText.setMessage(PASSWORD_SET_MESSAGE);
			} else {
				communicationPasswordText.setMessage(PASSWORD_NOT_SET_MESSAGE);
			}
			if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.LINUX) {
				communicationPasswordText.setEditable(preferences.isEncryptCommunication());
			} else {
				communicationPasswordText.setEnabled(preferences.isEncryptCommunication());
			}
			communicationPasswordText.setToolTipText("Type the security passphrase set in your device");
			communicationPasswordText.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					// Do nothing
				}

				@Override
				public void keyReleased(KeyEvent e) {
					communicationPasswordChanged = true;
					String password = communicationPasswordText.getText();
					setCommunicationPassword(password);
				}
			});
			communicationPasswordText.addListener(SWT.FocusIn, new Listener() {
				@Override
				public void handleEvent(Event event) {
					communicationPasswordChanged = false;
				}
			});
			communicationPasswordText.addListener(SWT.FocusOut, new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (communicationPasswordChanged) {
						String password = communicationPasswordText.getText();
						setCommunicationPassword(password);
						if (communicationPasswordText.getText().length() == 0) {
							communicationPasswordText.setMessage(PASSWORD_NOT_SET_MESSAGE);
						} else {
							communicationPasswordText.setMessage(PASSWORD_SET_MESSAGE);
						}
					}
					communicationPasswordText.setText("");
				}
			});
			encryptCommunicationCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					boolean enabled = encryptCommunicationCheckbox.getSelection();
					if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.LINUX) {
						communicationPasswordText.setEditable(enabled);
					} else {
						communicationPasswordText.setEnabled(enabled);
					}
				}
			});
			notificationReceptionMethodsGroup.setExpanded(preferences.isGroupExpanded(ApplicationPreferences.Group.RECEPTION));

			// Notification Display Group

			notificationDisplayMethodsGroup = createPGroup();
			GridLayout notificationDisplayMethodsGroupLayout = new GridLayout();
			notificationDisplayMethodsGroupLayout.numColumns = 2;
			notificationDisplayMethodsGroup.setLayout(notificationDisplayMethodsGroupLayout);
			FormData notificationDisplayMethodsGroupLData = new FormData();
			notificationDisplayMethodsGroupLData.top = new FormAttachment(notificationReceptionMethodsGroup, 2);
			notificationDisplayMethodsGroupLData.left = new FormAttachment(0, 10);
			notificationDisplayMethodsGroupLData.right = new FormAttachment(100, -10);
			notificationDisplayMethodsGroup.setLayoutData(notificationDisplayMethodsGroupLData);
			notificationDisplayMethodsGroup.setText("Notification Display Methods");
			notificationDisplayMethodsGroup.addExpandListener(new GroupExpandListener(ApplicationPreferences.Group.DISPLAY));

			systemDefaultCheckbox = new Button(notificationDisplayMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData systemDefaultCheckboxLData = new GridData();
			systemDefaultCheckboxLData.horizontalIndent = 5;
			systemDefaultCheckboxLData.horizontalSpan = 2;
			systemDefaultCheckbox.setLayoutData(systemDefaultCheckboxLData);
			systemDefaultCheckbox.setText("System default");
			systemDefaultCheckbox.setToolTipText("Use system default mechanism to show notifications");
			systemDefaultCheckbox.setSelection(preferences.isDisplayWithSystemDefault());
			systemDefaultCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = systemDefaultCheckbox.getSelection();
					Future<Service.State> future = application.adjustSystemDefaultBroadcaster(enabled);
					new ServiceAdjustListener(future, systemDefaultCheckbox, new PreferenceSetter() {
						@Override
						public void onSuccess() {
							preferences.setDisplayWithSystemDefault(enabled);
						}

						@Override
						public void onError() {
							preferences.setDisplayWithSystemDefault(false);
						}
					}).start();
				}
			});

			growlCheckbox = new Button(notificationDisplayMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData growlCheckboxLData = new GridData();
			growlCheckboxLData.horizontalIndent = 5;
			growlCheckboxLData.horizontalSpan = 2;
			growlCheckbox.setLayoutData(growlCheckboxLData);
			growlCheckbox.setText("Growl Notification Transport Protocol");
			growlCheckbox.setSelection(preferences.isDisplayWithGrowl());
			growlCheckbox.setToolTipText("Use GNTP to show notifications, network notifications must be enabled in Growl, see how-to on the project wiki to setup");
			growlCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = growlCheckbox.getSelection();
					Future<Service.State> future = application.adjustGrowlBroadcaster(enabled);
					new ServiceAdjustListener(future, growlCheckbox, new PreferenceSetter() {
						@Override
						public void onSuccess() {
							preferences.setDisplayWithGrowl(enabled);
						}

						@Override
						public void onError() {
							preferences.setDisplayWithGrowl(false);
						}
					}).start();
				}
			});

			if (OperatingSystems.CURRENT_FAMILY == Family.LINUX) {
				libnotifyCheckbox = new Button(notificationDisplayMethodsGroup, SWT.CHECK | SWT.LEFT);
				GridData libnotifyCheckboxLData = new GridData();
				libnotifyCheckboxLData.horizontalIndent = 5;
				libnotifyCheckboxLData.horizontalSpan = 2;
				libnotifyCheckbox.setLayoutData(libnotifyCheckboxLData);
				libnotifyCheckbox.setText("Libnotify");
				libnotifyCheckbox.setToolTipText("Use notify-send command to show notifications");
				libnotifyCheckbox.setSelection(preferences.isDisplayWithLibnotify());
				libnotifyCheckbox.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						final boolean enabled = libnotifyCheckbox.getSelection();
						Future<Service.State> future = application.adjustLibnotifyBroadcaster(enabled);
						new ServiceAdjustListener(future, libnotifyCheckbox, new PreferenceSetter() {
							@Override
							public void onSuccess() {
								preferences.setDisplayWithLibnotify(enabled);
							}

							@Override
							public void onError() {
								preferences.setDisplayWithLibnotify(false);
							}
						}).start();
					}
				});
			}

			msnCheckbox = new Button(notificationDisplayMethodsGroup, SWT.CHECK | SWT.LEFT);
			GridData msnCheckboxLData = new GridData();
			msnCheckboxLData.horizontalIndent = 5;
			msnCheckbox.setLayoutData(msnCheckboxLData);
			msnCheckbox.setText("Windows Live Messenger IM");
			msnCheckbox.setSelection(preferences.isDisplayWithMsn());
			msnCheckbox.setToolTipText("Send notifications over Windows Live instant messaging");
			msnCheckbox.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final boolean enabled = msnCheckbox.getSelection();
					Future<Service.State> future = application.adjustMsnBroadcaster(enabled);
					new ServiceAdjustListener(future, msnCheckbox, new PreferenceSetter() {
						@Override
						public void onSuccess() {
							preferences.setDisplayWithMsn(enabled);
						}

						@Override
						public void onError() {
							preferences.setDisplayWithMsn(false);
						}
					}).start();
				}
			});

			Button msnDetailButton = new Button(notificationDisplayMethodsGroup, SWT.PUSH | SWT.CENTER);
			GridData msnDetailButtonLData = new GridData();
			msnDetailButton.setLayoutData(msnDetailButtonLData);
			msnDetailButton.setText("Details...");
			msnDetailButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					InstantMessagingDetailDialog dialog = new InstantMessagingDetailDialog(swtManager, preferences.getMsnUsername(), preferences.getMsnPassword(), preferences.getMsnTarget());
					dialog.open(new InstantMessagingDetailDialog.SubmitListener() {
						@Override
						public void onTest(String username, String password, String target) {
							// TODO
						}

						@Override
						public void onSubmit(String username, String password, String target) {
							preferences.setMsnUsername(username);
							preferences.setMsnPassword(password);
							preferences.setMsnTarget(target);
						}
					});
				}
			});
			
			notificationDisplayMethodsGroup.setExpanded(preferences.isGroupExpanded(ApplicationPreferences.Group.DISPLAY));

			// Notification Actions Groups
			notificationActionsGroup = createPGroup();
			FillLayout notificationActionsGroupLayout = new FillLayout();
			notificationActionsGroupLayout.marginHeight = 5;
			notificationActionsGroup.setLayout(notificationActionsGroupLayout);
			FormData notificationActionsGroupLData = new FormData();
			notificationActionsGroupLData.top = new FormAttachment(notificationDisplayMethodsGroup, 2);
			notificationActionsGroupLData.left = new FormAttachment(0, 10);
			notificationActionsGroupLData.right = new FormAttachment(100, -10);
			notificationActionsGroup.setLayoutData(notificationActionsGroupLData);
			notificationActionsGroup.setText("Notification Actions");
			notificationActionsGroup.addExpandListener(new GroupExpandListener(ApplicationPreferences.Group.ACTION));

			notificationTypesTabFolder = new TabFolder(notificationActionsGroup, SWT.NONE);
			for (Notification.Type type : Notification.Type.values()) {
				createNotificationTypeTabItem(notificationTypesTabFolder, type);
			}
			notificationTypesTabFolder.pack();
			notificationActionsGroup.setExpanded(preferences.isGroupExpanded(ApplicationPreferences.Group.ACTION));

			// Devices Group

			devicesGroup = createPGroup();
			GridLayout devicesGroupLayout = new GridLayout();
			devicesGroupLayout.numColumns = 2;
			devicesGroup.setLayout(devicesGroupLayout);
			FormData devicesGroupLData = new FormData();
			devicesGroupLData.top = new FormAttachment(notificationActionsGroup, 2);
			devicesGroupLData.left = new FormAttachment(0, 10);
			devicesGroupLData.right = new FormAttachment(100, -10);
			devicesGroup.setLayoutData(devicesGroupLData);
			devicesGroup.setText("Devices");
			devicesGroup.addExpandListener(new GroupExpandListener(ApplicationPreferences.Group.PAIRING));

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
			pairedDevices.putAll(preferences.getAllowedDevices());
			for (String name : pairedDevices.values()) {
				devicesList.add(name);
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
						public void onPairingSuccessful(final long deviceId) {
							if (!preferences.getAllowedDevicesIds().contains(deviceId)) {
								notificationManager.cancelWaitForPairing();
								swtManager.update(new Runnable() {
									@Override
									public void run() {
										dialog.close();
										final DeviceEditorDialog deviceDialog = new DeviceEditorDialog(swtManager, deviceId, "My Android");
										deviceDialog.open(new DeviceEditorDialog.SubmitListener() {
											@Override
											public boolean onDeviceName(String name) {
												if (preferences.getAllowedDevicesNames().contains(name)) {
													return false;
												}
												preferences.addAllowedDeviceId(deviceId, name);
												pairedDevices.put(deviceId, name);
												devicesList.add(name);
												notificationManager.setPairedDevices(preferences.getAllowedDevices());
												deviceDialog.close();
												return true;
											}
										});
									}
								});
							}
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
						String deviceName = items[0];
						long deviceId = pairedDevices.inverse().get(deviceName);
						preferences.removeAllowedDeviceId(deviceId, deviceName);
						devicesList.remove(deviceName);
						notificationManager.setPairedDevices(preferences.getAllowedDevices());
					}
				}
			});
			devicesGroup.setExpanded(preferences.isGroupExpanded(ApplicationPreferences.Group.PAIRING));

			// OK/Cancel Buttons

			okButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
			FormData okButtonLData = new FormData();
			okButtonLData.width = 70;
			okButtonLData.height = 28;
			okButtonLData.top = new FormAttachment(devicesGroup, 6);
			okButtonLData.right = new FormAttachment(100, -10);
			okButton.setLayoutData(okButtonLData);
			okButton.setText("Save");
			okButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					dialogShell.close();
				}
			});

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

	protected PGroup createPGroup() {
		PGroup group = new PGroup(dialogShell, SWT.SMOOTH);
		group.setStrategy(new SimpleGroupStrategy());
		group.setToggleRenderer(new TreeNodeToggleRenderer());
		
		return group;
	}

	protected void setCommunicationPassword(String password) {
		byte[] key = password.length() == 0 ? new byte[0] : Encryption.passPhraseToKey(password);
		notificationParser.setEncryption(true, key);
		preferences.setCommunicationPassword(key);
	}

	class ServiceAdjustListener implements Runnable {
		private final Future<Service.State> future;
		private final Button checkbox;
		private final PreferenceSetter setter;

		public ServiceAdjustListener(Future<Service.State> future, Button checkbox, PreferenceSetter setter) {
			this.future = future;
			this.checkbox = checkbox;
			this.setter = setter;
		}

		public void start() {
			if (future != null) {
				Futures.makeListenable(future).addListener(this, executorService);
			}
		}

		@Override
		public void run() {
			try {
				future.get();
				setter.onSuccess();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
				swtManager.update(new Runnable() {
					@Override
					public void run() {
						if (!checkbox.isDisposed()) {
							checkbox.setSelection(false);
						}
					}
				});
				setter.onError();
			}
		}
	}

	interface PreferenceSetter {
		void onSuccess();
		void onError();
	}

	class GroupExpandListener implements ExpandListener {
		
		private final ApplicationPreferences.Group group;

		public GroupExpandListener(Group group) {
			this.group = group;
		}

		@Override
		public void itemCollapsed(ExpandEvent e) {
			preferences.setGroupExpanded(group, false);
			dialogShell.pack();
		}

		@Override
		public void itemExpanded(ExpandEvent e) {
			preferences.setGroupExpanded(group, true);
			dialogShell.pack();
		}
	}
}
