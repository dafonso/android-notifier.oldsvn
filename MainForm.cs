
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Resources;
using System.Threading;
using System.Windows.Forms;
using org.chieke.WinDroidNotifier;
using Growl.Connector;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of MainForm.
	/// </summary>
	public partial class MainForm : Form {
		private Thread mReceiverThread = null;
		private UDPReceiver mReceiver = null;
		private delegate void MessageLogDelegate();

		private bool mGrowlSupport = false;
		private Growl.Connector.GrowlConnector growl = null;
		private Growl.Connector.NotificationType notificationType = null;
		private Growl.Connector.Application application = null;
		private const string mNotificationType = "WINDROID_NOTIFICATION";
		private const string GROWL_TITLE = "android notifier";
		private string GROWL_EXECUTABLE = System.Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles) + "\\Growl for Windows\\Growl.exe";

		public MainForm() {
			//
			// The InitializeComponent() call is required for Windows Forms designer support.
			//
			InitializeComponent();

			lblReceiveState.Text = String.Empty;
			#if (!DEBUG)
			btnSampleNotification.Visible = false;
			#endif
		}

		private void WriteDebugMessage(string message) {
			#if (DEBUG)
			Console.WriteLine(message);
			#endif
		}

        private void MainFormLoad(object sender, EventArgs e) {
			bool preferencesLoaded = false;

			try {
				preferencesLoaded = NotifierPreferences.Load();
			} catch (System.IO.FileNotFoundException) {
				// ignore this on start if there is not preference file, we'll create it later
			}

			if (preferencesLoaded) {
				#if (!DEBUG)
				this.WindowState = FormWindowState.Minimized;
				#endif
			}

			// check if autorun is set in windows registry
			string keyName = "WinDroidNotifier";
			string assemblyLocation = System.Windows.Forms.Application.ExecutablePath;
			chkStartAtLogin.Checked = AutorunHelper.IsAutoStartEnabled(keyName, assemblyLocation);

			// growl support
			mGrowlSupport = GrowlSupportAvailable();
			chkUseGrowl.Enabled = mGrowlSupport;
			if (!mGrowlSupport && NotifierPreferences.preferences.UseGrowl) {
				NotifierPreferences.preferences.UseGrowl = false;
			}

			if (NotifierPreferences.preferences.UseGrowl) {
				this.InitGrowl();
			}

			// set preferences in GUI
			chkUseGrowl.Checked = NotifierPreferences.preferences.UseGrowl;
			linkDownloadGrowl.Visible = !mGrowlSupport;
			
			ListViewItem lvi = new ListViewItem();
			lvi.Text = "device_id_1";
			lvi.SubItems.Add("device name 1");
			lvwReceiveDevice.Items.Add(lvi);

			lvi = new ListViewItem();
			lvi.Text = "device_id_2";
			lvi.SubItems.Add("device name 2");
			lvwReceiveDevice.Items.Add(lvi);

			this.StartReceiver();
		}

		private void MainFormFormClosing(object sender, FormClosingEventArgs e) {
			if (mReceiverThread != null) {
				if (mReceiver != null) {
					mReceiver.Stop();
					mReceiverThread.Abort();
				}
			}
		}

		private bool GrowlSupportAvailable() {
			return System.IO.File.Exists(GROWL_EXECUTABLE);
		}

		private void InitGrowl() {
			this.notificationType = new NotificationType(mNotificationType, "WinDroidNotifier");
			this.growl = new GrowlConnector();

			if (!this.growl.IsGrowlRunning()) {
				this.WriteDebugMessage("Growl is not running. Start process...");
				System.Diagnostics.Process.Start(GROWL_EXECUTABLE);
			}
        }

		private void StartReceiver() {
			this.WriteDebugMessage("Start listening...");

			lblReceiveState.Text = "Listening for events";
			btnStartReceive.Enabled = false;
			btnStopReceive.Enabled = true;
			mReceiverThread = new Thread(new ThreadStart(ReceiverThread));
			mReceiverThread.Start();
		}

		private void ReceiverThread() {
			mReceiver = new UDPReceiver();
    		IEventReceiverInterface rec = (IEventReceiverInterface)mReceiver;
    		rec.OnNotificationReceived += new EventHandler(rec_OnNotificationReceived);
    		mReceiver.Start();
		}

		private void rec_OnNotificationReceived(object sender, EventArgs e) {
			this.WriteDebugMessage(((NotificationArgs)e).CompleteNotification);

			this.WriteDebugMessage("\tType: " + ((NotificationArgs)e).NotificationType.ToString());
			this.WriteDebugMessage("\tData: " + ((NotificationArgs)e).EventContents);
			this.WriteDebugMessage(System.Environment.NewLine);

			string title = ((NotificationArgs)e).NotificationType.ToString();
			string message = ((NotificationArgs)e).EventContents;

			if (mGrowlSupport && NotifierPreferences.preferences.UseGrowl) {
				this.DisplayGrowlNotification(title, message, ((NotificationArgs)e));
			} else {
				notifyIcon.ShowBalloonTip(2000, title, message, ToolTipIcon.Info);
			}
		}

		private void DisplayGrowlNotification(string title, string message, NotificationArgs evargs) {
			string notifyIcon = System.Windows.Forms.Application.StartupPath + "\\Resources\\";
			string icon = null;

			if (evargs != null) {
				switch (evargs.NotificationType) {
					case Enums.EventType.BATTERY:
						icon = "growl_app_icon_battery.png";
						break;
					case Enums.EventType.MMS:
						icon = "growl_app_icon_mms.png";
						break;
					case Enums.EventType.RING:
						icon = "growl_app_icon_ring.png";
						break;
					case Enums.EventType.SMS:
						icon = "growl_app_icon_sms.png";
						break;
					case Enums.EventType.PING:
					default:
						icon = "growl_app_icon.png";
						break;
				}
			}

			if (String.IsNullOrEmpty(icon)) {
				icon = "growl_app_icon.png";
			}
			notifyIcon += icon;

			this.application = new Growl.Connector.Application(GROWL_TITLE);
			this.application.Icon = notifyIcon;
			this.growl.Register(application, new NotificationType[] { notificationType });

			Notification notification = 
				new Notification(this.application.Name, this.notificationType.Name, DateTime.Now.Ticks.ToString(), title, message);
			this.growl.Notify(notification);
		}

		private void BtnOverlayClick(object sender, EventArgs e) {
			if (mGrowlSupport && NotifierPreferences.preferences.UseGrowl) {
				this.DisplayGrowlNotification("My Title", "My Message", null);
			} else {
				notifyIcon.ShowBalloonTip(2000, "My Title", "My Message", ToolTipIcon.Info);
			}
		}

		#region GUI Events

		private void BtnStartReceiveClick(object sender, EventArgs e) {
			this.StartReceiver();
		}

		private void BtnStopReceiveClick(object sender, EventArgs e) {
        	this.WriteDebugMessage("Stop listening");

			lblReceiveState.Text = String.Empty;
			btnStartReceive.Enabled = true;
			btnStopReceive.Enabled = false;

			if (mReceiver != null) {
				mReceiver.Stop();

				if (mReceiverThread != null) {
					if (mReceiverThread.IsAlive) {
						mReceiverThread.Abort();

						mReceiverThread = null;
					}
				}
			}
		}

		private void ChkStartAtLoginCheckedChanged(object sender, EventArgs e) {
			string keyName = "WinDroidNotifier";
			string assemblyLocation = System.Windows.Forms.Application.ExecutablePath;

			if (!chkStartAtLogin.Checked && AutorunHelper.IsAutoStartEnabled(keyName, assemblyLocation)) {
				AutorunHelper.UnSetAutoStart(keyName);
			}

			if (chkStartAtLogin.Checked && !AutorunHelper.IsAutoStartEnabled(keyName, assemblyLocation)) {
				AutorunHelper.SetAutoStart(keyName, assemblyLocation);
			}
		}

		private void ChkUseGrowlCheckedChanged(object sender, EventArgs e) {
			NotifierPreferences.preferences.UseGrowl = chkUseGrowl.Checked;
			NotifierPreferences.Save();

			if (this.growl == null) {
				this.InitGrowl();
			}
		}

		private void LinkDownloadGrowlLinkClicked(object sender, LinkLabelLinkClickedEventArgs e) {
			System.Diagnostics.Process.Start("http://www.growlforwindows.com");
		}

		private void ExitToolStripMenuItemClick(object sender, EventArgs e) {
			System.Windows.Forms.Application.Exit();
		}

		private void PreferencesToolStripMenuItemClick(object sender, EventArgs e) {
			this.WindowState = FormWindowState.Normal;
		}

		private void MainFormSizeChanged(object sender, EventArgs e) {
			if (this.WindowState == FormWindowState.Minimized) {
				this.ShowInTaskbar = false;
			} else {
				this.ShowInTaskbar = true;
			}
		}

		private void NotifyIconMouseClick(object sender, MouseEventArgs e) {
			if (e.Button == MouseButtons.Left) {
				this.WindowState = FormWindowState.Normal;
				this.ShowInTaskbar = true;
			}
		}

		private void LvwReceiveDeviceSelectedIndexChanged(object sender, EventArgs e) {
			btnRemoveDevice.Enabled = (lvwReceiveDevice.SelectedItems != null && lvwReceiveDevice.SelectedItems.Count > 0);
		}
		
		private void LvwReceiveDeviceDoubleClick(object sender, EventArgs e) {
			if (lvwReceiveDevice.SelectedItems != null && lvwReceiveDevice.SelectedItems.Count == 1) {
				AddDevice ad = new AddDevice();
				ad.DeviceID = lvwReceiveDevice.SelectedItems[0].SubItems[0].Text;
				ad.DeviceName = lvwReceiveDevice.SelectedItems[0].SubItems[1].Text;

				if (ad.ShowDialog() == DialogResult.OK) {
					lvwReceiveDevice.SelectedItems[0].SubItems[0].Text = ad.DeviceID;
					lvwReceiveDevice.SelectedItems[0].SubItems[1].Text = ad.DeviceName;
				}
			}
		}

		private void BtnRemoveDeviceClick(object sender, EventArgs e) {
			if (lvwReceiveDevice.SelectedItems != null && lvwReceiveDevice.SelectedItems.Count > 0) {
				foreach (ListViewItem lvi in lvwReceiveDevice.SelectedItems) {
					lvwReceiveDevice.Items.Remove(lvi);
				}
			}
		}

		private void BtnAddDeviceClick(object sender, EventArgs e) {
			AddDevice ad = new AddDevice();

			if (ad.ShowDialog() == DialogResult.OK) {
				ListViewItem lvi = new ListViewItem();
				lvi.Text = ad.DeviceID;
				lvi.SubItems.Add(ad.DeviceName);
				lvwReceiveDevice.Items.Add(lvi);
			}
		}

		private void RbReceiveAnyDeviceCheckedChanged(object sender, EventArgs e) {
			lvwReceiveDevice.Enabled = !rbReceiveAnyDevice.Checked;
		}

		#endregion
	}
}
