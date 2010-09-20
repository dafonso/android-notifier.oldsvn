
namespace org.chieke.WinDroidNotifier
{
	partial class MainForm
	{
		/// <summary>
		/// Designer variable used to keep track of non-visual components.
		/// </summary>
		private System.ComponentModel.IContainer components = null;
		
		/// <summary>
		/// Disposes resources used by the form.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing) {
				if (components != null) {
					components.Dispose();
				}
			}
			base.Dispose(disposing);
		}
		
		/// <summary>
		/// This method is required for Windows Forms designer support.
		/// Do not change the method contents inside the source code editor. The Forms designer might
		/// not be able to load this method if it was changed manually.
		/// </summary>
		private void InitializeComponent()
		{
			this.components = new System.ComponentModel.Container();
			System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainForm));
			this.btnStartReceive = new System.Windows.Forms.Button();
			this.btnStopReceive = new System.Windows.Forms.Button();
			this.lblReceiveState = new System.Windows.Forms.Label();
			this.btnSampleNotification = new System.Windows.Forms.Button();
			this.groupGeneralOptions = new System.Windows.Forms.GroupBox();
			this.chkStartAtLogin = new System.Windows.Forms.CheckBox();
			this.groupNotifications = new System.Windows.Forms.GroupBox();
			this.panel3 = new System.Windows.Forms.Panel();
			this.linkDownloadGrowl = new System.Windows.Forms.LinkLabel();
			this.chkUseGrowl = new System.Windows.Forms.CheckBox();
			this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
			this.label6 = new System.Windows.Forms.Label();
			this.label7 = new System.Windows.Forms.Label();
			this.label8 = new System.Windows.Forms.Label();
			this.label9 = new System.Windows.Forms.Label();
			this.label4 = new System.Windows.Forms.Label();
			this.checkBox1 = new System.Windows.Forms.CheckBox();
			this.checkBox2 = new System.Windows.Forms.CheckBox();
			this.checkBox3 = new System.Windows.Forms.CheckBox();
			this.checkBox4 = new System.Windows.Forms.CheckBox();
			this.checkBox5 = new System.Windows.Forms.CheckBox();
			this.label1 = new System.Windows.Forms.Label();
			this.lblNotificationActionsDescription = new System.Windows.Forms.Label();
			this.lblNotificationActions = new System.Windows.Forms.Label();
			this.panel2 = new System.Windows.Forms.Panel();
			this.label5 = new System.Windows.Forms.Label();
			this.btnRemoveDevice = new System.Windows.Forms.Button();
			this.btnAddDevice = new System.Windows.Forms.Button();
			this.lvwReceiveDevice = new System.Windows.Forms.ListView();
			this.colDeviceID = new System.Windows.Forms.ColumnHeader();
			this.colDeviceName = new System.Windows.Forms.ColumnHeader();
			this.rbReceiveSpecifiedDevice = new System.Windows.Forms.RadioButton();
			this.rbReceiveAnyDevice = new System.Windows.Forms.RadioButton();
			this.lblDevicesDescription = new System.Windows.Forms.Label();
			this.lblDevices = new System.Windows.Forms.Label();
			this.notifyIcon = new System.Windows.Forms.NotifyIcon(this.components);
			this.contextNotify = new System.Windows.Forms.ContextMenuStrip(this.components);
			this.preferencesToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.groupGeneralOptions.SuspendLayout();
			this.groupNotifications.SuspendLayout();
			this.panel3.SuspendLayout();
			this.tableLayoutPanel1.SuspendLayout();
			this.panel2.SuspendLayout();
			this.contextNotify.SuspendLayout();
			this.SuspendLayout();
			// 
			// btnStartReceive
			// 
			this.btnStartReceive.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
			this.btnStartReceive.BackColor = System.Drawing.SystemColors.Control;
			this.btnStartReceive.Location = new System.Drawing.Point(196, 19);
			this.btnStartReceive.Name = "btnStartReceive";
			this.btnStartReceive.Size = new System.Drawing.Size(75, 23);
			this.btnStartReceive.TabIndex = 1;
			this.btnStartReceive.Text = "Start";
			this.btnStartReceive.UseVisualStyleBackColor = false;
			this.btnStartReceive.Click += new System.EventHandler(this.BtnStartReceiveClick);
			// 
			// btnStopReceive
			// 
			this.btnStopReceive.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
			this.btnStopReceive.BackColor = System.Drawing.SystemColors.Control;
			this.btnStopReceive.Enabled = false;
			this.btnStopReceive.Location = new System.Drawing.Point(277, 19);
			this.btnStopReceive.Name = "btnStopReceive";
			this.btnStopReceive.Size = new System.Drawing.Size(75, 23);
			this.btnStopReceive.TabIndex = 5;
			this.btnStopReceive.Text = "Stop";
			this.btnStopReceive.UseVisualStyleBackColor = false;
			this.btnStopReceive.Click += new System.EventHandler(this.BtnStopReceiveClick);
			// 
			// lblReceiveState
			// 
			this.lblReceiveState.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
			this.lblReceiveState.AutoSize = true;
			this.lblReceiveState.BackColor = System.Drawing.Color.Transparent;
			this.lblReceiveState.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.lblReceiveState.Location = new System.Drawing.Point(12, 403);
			this.lblReceiveState.Name = "lblReceiveState";
			this.lblReceiveState.Size = new System.Drawing.Size(89, 13);
			this.lblReceiveState.TabIndex = 3;
			this.lblReceiveState.Text = "[receive state]";
			// 
			// btnSampleNotification
			// 
			this.btnSampleNotification.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.btnSampleNotification.BackColor = System.Drawing.SystemColors.Control;
			this.btnSampleNotification.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
			this.btnSampleNotification.Location = new System.Drawing.Point(262, 398);
			this.btnSampleNotification.Name = "btnSampleNotification";
			this.btnSampleNotification.Size = new System.Drawing.Size(108, 23);
			this.btnSampleNotification.TabIndex = 15;
			this.btnSampleNotification.Text = "Sample Notification";
			this.btnSampleNotification.UseVisualStyleBackColor = false;
			this.btnSampleNotification.Click += new System.EventHandler(this.BtnOverlayClick);
			// 
			// groupGeneralOptions
			// 
			this.groupGeneralOptions.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
									| System.Windows.Forms.AnchorStyles.Right)));
			this.groupGeneralOptions.Controls.Add(this.chkStartAtLogin);
			this.groupGeneralOptions.Controls.Add(this.btnStartReceive);
			this.groupGeneralOptions.Controls.Add(this.btnStopReceive);
			this.groupGeneralOptions.Location = new System.Drawing.Point(12, 12);
			this.groupGeneralOptions.Name = "groupGeneralOptions";
			this.groupGeneralOptions.Size = new System.Drawing.Size(358, 52);
			this.groupGeneralOptions.TabIndex = 8;
			this.groupGeneralOptions.TabStop = false;
			this.groupGeneralOptions.Text = "General options";
			// 
			// chkStartAtLogin
			// 
			this.chkStartAtLogin.AutoSize = true;
			this.chkStartAtLogin.Location = new System.Drawing.Point(6, 23);
			this.chkStartAtLogin.Name = "chkStartAtLogin";
			this.chkStartAtLogin.Size = new System.Drawing.Size(85, 17);
			this.chkStartAtLogin.TabIndex = 0;
			this.chkStartAtLogin.Text = "Start at login";
			this.chkStartAtLogin.UseVisualStyleBackColor = true;
			this.chkStartAtLogin.CheckedChanged += new System.EventHandler(this.ChkStartAtLoginCheckedChanged);
			// 
			// groupNotifications
			// 
			this.groupNotifications.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
									| System.Windows.Forms.AnchorStyles.Left) 
									| System.Windows.Forms.AnchorStyles.Right)));
			this.groupNotifications.Controls.Add(this.panel3);
			this.groupNotifications.Controls.Add(this.panel2);
			this.groupNotifications.Location = new System.Drawing.Point(12, 70);
			this.groupNotifications.Name = "groupNotifications";
			this.groupNotifications.Size = new System.Drawing.Size(358, 310);
			this.groupNotifications.TabIndex = 9;
			this.groupNotifications.TabStop = false;
			this.groupNotifications.Text = "Notifications";
			// 
			// panel3
			// 
			this.panel3.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
									| System.Windows.Forms.AnchorStyles.Right)));
			this.panel3.Controls.Add(this.linkDownloadGrowl);
			this.panel3.Controls.Add(this.chkUseGrowl);
			this.panel3.Controls.Add(this.tableLayoutPanel1);
			this.panel3.Controls.Add(this.lblNotificationActionsDescription);
			this.panel3.Controls.Add(this.lblNotificationActions);
			this.panel3.Location = new System.Drawing.Point(10, 19);
			this.panel3.Name = "panel3";
			this.panel3.Size = new System.Drawing.Size(342, 84);
			this.panel3.TabIndex = 2;
			// 
			// linkDownloadGrowl
			// 
			this.linkDownloadGrowl.ActiveLinkColor = System.Drawing.Color.Blue;
			this.linkDownloadGrowl.AutoSize = true;
			this.linkDownloadGrowl.LinkColor = System.Drawing.Color.Blue;
			this.linkDownloadGrowl.Location = new System.Drawing.Point(158, 20);
			this.linkDownloadGrowl.Name = "linkDownloadGrowl";
			this.linkDownloadGrowl.Size = new System.Drawing.Size(85, 13);
			this.linkDownloadGrowl.TabIndex = 4;
			this.linkDownloadGrowl.TabStop = true;
			this.linkDownloadGrowl.Text = "Download Growl";
			this.linkDownloadGrowl.Visible = false;
			this.linkDownloadGrowl.VisitedLinkColor = System.Drawing.Color.Blue;
			this.linkDownloadGrowl.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.LinkDownloadGrowlLinkClicked);
			// 
			// chkUseGrowl
			// 
			this.chkUseGrowl.AutoSize = true;
			this.chkUseGrowl.Location = new System.Drawing.Point(3, 16);
			this.chkUseGrowl.Name = "chkUseGrowl";
			this.chkUseGrowl.Size = new System.Drawing.Size(149, 17);
			this.chkUseGrowl.TabIndex = 3;
			this.chkUseGrowl.Text = "Use Growl for notifications";
			this.chkUseGrowl.UseVisualStyleBackColor = true;
			this.chkUseGrowl.CheckedChanged += new System.EventHandler(this.ChkUseGrowlCheckedChanged);
			// 
			// tableLayoutPanel1
			// 
			this.tableLayoutPanel1.AutoSize = true;
			this.tableLayoutPanel1.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
			this.tableLayoutPanel1.ColumnCount = 6;
			this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
			this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
			this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
			this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
			this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
			this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
			this.tableLayoutPanel1.Controls.Add(this.label6, 2, 0);
			this.tableLayoutPanel1.Controls.Add(this.label7, 3, 0);
			this.tableLayoutPanel1.Controls.Add(this.label8, 4, 0);
			this.tableLayoutPanel1.Controls.Add(this.label9, 5, 0);
			this.tableLayoutPanel1.Controls.Add(this.label4, 1, 0);
			this.tableLayoutPanel1.Controls.Add(this.checkBox1, 1, 1);
			this.tableLayoutPanel1.Controls.Add(this.checkBox2, 2, 1);
			this.tableLayoutPanel1.Controls.Add(this.checkBox3, 3, 1);
			this.tableLayoutPanel1.Controls.Add(this.checkBox4, 4, 1);
			this.tableLayoutPanel1.Controls.Add(this.checkBox5, 5, 1);
			this.tableLayoutPanel1.Controls.Add(this.label1, 0, 1);
			this.tableLayoutPanel1.Location = new System.Drawing.Point(3, 39);
			this.tableLayoutPanel1.Name = "tableLayoutPanel1";
			this.tableLayoutPanel1.RowCount = 2;
			this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
			this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
			this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
			this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
			this.tableLayoutPanel1.Size = new System.Drawing.Size(314, 33);
			this.tableLayoutPanel1.TabIndex = 6;
			// 
			// label6
			// 
			this.label6.AutoSize = true;
			this.label6.Location = new System.Drawing.Point(139, 0);
			this.label6.Name = "label6";
			this.label6.Size = new System.Drawing.Size(52, 13);
			this.label6.TabIndex = 8;
			this.label6.Text = "Voicemail";
			// 
			// label7
			// 
			this.label7.AutoSize = true;
			this.label7.Location = new System.Drawing.Point(197, 0);
			this.label7.Name = "label7";
			this.label7.Size = new System.Drawing.Size(30, 13);
			this.label7.TabIndex = 9;
			this.label7.Text = "SMS";
			// 
			// label8
			// 
			this.label8.AutoSize = true;
			this.label8.Location = new System.Drawing.Point(233, 0);
			this.label8.Name = "label8";
			this.label8.Size = new System.Drawing.Size(32, 13);
			this.label8.TabIndex = 10;
			this.label8.Text = "MMS";
			// 
			// label9
			// 
			this.label9.AutoSize = true;
			this.label9.Location = new System.Drawing.Point(271, 0);
			this.label9.Name = "label9";
			this.label9.Size = new System.Drawing.Size(40, 13);
			this.label9.TabIndex = 11;
			this.label9.Text = "Battery";
			// 
			// label4
			// 
			this.label4.AutoSize = true;
			this.label4.Location = new System.Drawing.Point(104, 0);
			this.label4.Name = "label4";
			this.label4.Size = new System.Drawing.Size(29, 13);
			this.label4.TabIndex = 7;
			this.label4.Text = "Ring";
			// 
			// checkBox1
			// 
			this.checkBox1.AutoSize = true;
			this.checkBox1.Location = new System.Drawing.Point(104, 16);
			this.checkBox1.Name = "checkBox1";
			this.checkBox1.Size = new System.Drawing.Size(15, 14);
			this.checkBox1.TabIndex = 5;
			this.checkBox1.UseVisualStyleBackColor = true;
			// 
			// checkBox2
			// 
			this.checkBox2.AutoSize = true;
			this.checkBox2.Location = new System.Drawing.Point(139, 16);
			this.checkBox2.Name = "checkBox2";
			this.checkBox2.Size = new System.Drawing.Size(15, 14);
			this.checkBox2.TabIndex = 6;
			this.checkBox2.UseVisualStyleBackColor = true;
			// 
			// checkBox3
			// 
			this.checkBox3.AutoSize = true;
			this.checkBox3.Location = new System.Drawing.Point(197, 16);
			this.checkBox3.Name = "checkBox3";
			this.checkBox3.Size = new System.Drawing.Size(15, 14);
			this.checkBox3.TabIndex = 7;
			this.checkBox3.UseVisualStyleBackColor = true;
			// 
			// checkBox4
			// 
			this.checkBox4.AutoSize = true;
			this.checkBox4.Location = new System.Drawing.Point(233, 16);
			this.checkBox4.Name = "checkBox4";
			this.checkBox4.Size = new System.Drawing.Size(15, 14);
			this.checkBox4.TabIndex = 8;
			this.checkBox4.UseVisualStyleBackColor = true;
			// 
			// checkBox5
			// 
			this.checkBox5.AutoSize = true;
			this.checkBox5.Location = new System.Drawing.Point(271, 16);
			this.checkBox5.Name = "checkBox5";
			this.checkBox5.Size = new System.Drawing.Size(15, 14);
			this.checkBox5.TabIndex = 9;
			this.checkBox5.UseVisualStyleBackColor = true;
			// 
			// label1
			// 
			this.label1.AutoSize = true;
			this.label1.Location = new System.Drawing.Point(3, 13);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(95, 13);
			this.label1.TabIndex = 2;
			this.label1.Text = "Display notification";
			// 
			// lblNotificationActionsDescription
			// 
			this.lblNotificationActionsDescription.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
									| System.Windows.Forms.AnchorStyles.Left) 
									| System.Windows.Forms.AnchorStyles.Right)));
			this.lblNotificationActionsDescription.Location = new System.Drawing.Point(0, 13);
			this.lblNotificationActionsDescription.Name = "lblNotificationActionsDescription";
			this.lblNotificationActionsDescription.Size = new System.Drawing.Size(339, 0);
			this.lblNotificationActionsDescription.TabIndex = 1;
			this.lblNotificationActionsDescription.Text = "For each event type, select which actions to take";
			// 
			// lblNotificationActions
			// 
			this.lblNotificationActions.AutoSize = true;
			this.lblNotificationActions.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.lblNotificationActions.Location = new System.Drawing.Point(0, 0);
			this.lblNotificationActions.Name = "lblNotificationActions";
			this.lblNotificationActions.Size = new System.Drawing.Size(117, 13);
			this.lblNotificationActions.TabIndex = 1;
			this.lblNotificationActions.Text = "Notification actions";
			// 
			// panel2
			// 
			this.panel2.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
									| System.Windows.Forms.AnchorStyles.Left) 
									| System.Windows.Forms.AnchorStyles.Right)));
			this.panel2.Controls.Add(this.label5);
			this.panel2.Controls.Add(this.btnRemoveDevice);
			this.panel2.Controls.Add(this.btnAddDevice);
			this.panel2.Controls.Add(this.lvwReceiveDevice);
			this.panel2.Controls.Add(this.rbReceiveSpecifiedDevice);
			this.panel2.Controls.Add(this.rbReceiveAnyDevice);
			this.panel2.Controls.Add(this.lblDevicesDescription);
			this.panel2.Controls.Add(this.lblDevices);
			this.panel2.Location = new System.Drawing.Point(6, 109);
			this.panel2.Name = "panel2";
			this.panel2.Size = new System.Drawing.Size(346, 195);
			this.panel2.TabIndex = 1;
			// 
			// label5
			// 
			this.label5.AutoSize = true;
			this.label5.Location = new System.Drawing.Point(175, 174);
			this.label5.Name = "label5";
			this.label5.Size = new System.Drawing.Size(148, 13);
			this.label5.TabIndex = 7;
			this.label5.Text = "(double-click a name to edit it)";
			// 
			// btnRemoveDevice
			// 
			this.btnRemoveDevice.Enabled = false;
			this.btnRemoveDevice.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.btnRemoveDevice.Location = new System.Drawing.Point(33, 169);
			this.btnRemoveDevice.Name = "btnRemoveDevice";
			this.btnRemoveDevice.Size = new System.Drawing.Size(23, 23);
			this.btnRemoveDevice.TabIndex = 14;
			this.btnRemoveDevice.Text = "-";
			this.btnRemoveDevice.UseVisualStyleBackColor = true;
			this.btnRemoveDevice.Click += new System.EventHandler(this.BtnRemoveDeviceClick);
			// 
			// btnAddDevice
			// 
			this.btnAddDevice.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.btnAddDevice.Location = new System.Drawing.Point(4, 169);
			this.btnAddDevice.Name = "btnAddDevice";
			this.btnAddDevice.Size = new System.Drawing.Size(23, 23);
			this.btnAddDevice.TabIndex = 13;
			this.btnAddDevice.Text = "+";
			this.btnAddDevice.UseVisualStyleBackColor = true;
			this.btnAddDevice.Click += new System.EventHandler(this.BtnAddDeviceClick);
			// 
			// lvwReceiveDevice
			// 
			this.lvwReceiveDevice.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
									this.colDeviceID,
									this.colDeviceName});
			this.lvwReceiveDevice.Enabled = false;
			this.lvwReceiveDevice.FullRowSelect = true;
			this.lvwReceiveDevice.Location = new System.Drawing.Point(4, 82);
			this.lvwReceiveDevice.Name = "lvwReceiveDevice";
			this.lvwReceiveDevice.Size = new System.Drawing.Size(331, 81);
			this.lvwReceiveDevice.TabIndex = 12;
			this.lvwReceiveDevice.UseCompatibleStateImageBehavior = false;
			this.lvwReceiveDevice.View = System.Windows.Forms.View.Details;
			this.lvwReceiveDevice.DoubleClick += new System.EventHandler(this.LvwReceiveDeviceDoubleClick);
			this.lvwReceiveDevice.SelectedIndexChanged += new System.EventHandler(this.LvwReceiveDeviceSelectedIndexChanged);
			// 
			// colDeviceID
			// 
			this.colDeviceID.Text = "Device ID";
			this.colDeviceID.Width = 141;
			// 
			// colDeviceName
			// 
			this.colDeviceName.Text = "Device Name";
			this.colDeviceName.Width = 173;
			// 
			// rbReceiveSpecifiedDevice
			// 
			this.rbReceiveSpecifiedDevice.AutoSize = true;
			this.rbReceiveSpecifiedDevice.Location = new System.Drawing.Point(4, 59);
			this.rbReceiveSpecifiedDevice.Name = "rbReceiveSpecifiedDevice";
			this.rbReceiveSpecifiedDevice.Size = new System.Drawing.Size(238, 17);
			this.rbReceiveSpecifiedDevice.TabIndex = 11;
			this.rbReceiveSpecifiedDevice.TabStop = true;
			this.rbReceiveSpecifiedDevice.Text = "Only receive notifications from these devices:";
			this.rbReceiveSpecifiedDevice.UseVisualStyleBackColor = true;
			// 
			// rbReceiveAnyDevice
			// 
			this.rbReceiveAnyDevice.AutoSize = true;
			this.rbReceiveAnyDevice.Checked = true;
			this.rbReceiveAnyDevice.Location = new System.Drawing.Point(4, 36);
			this.rbReceiveAnyDevice.Name = "rbReceiveAnyDevice";
			this.rbReceiveAnyDevice.Size = new System.Drawing.Size(202, 17);
			this.rbReceiveAnyDevice.TabIndex = 10;
			this.rbReceiveAnyDevice.TabStop = true;
			this.rbReceiveAnyDevice.Text = "Receive notifications from any device";
			this.rbReceiveAnyDevice.UseVisualStyleBackColor = true;
			this.rbReceiveAnyDevice.CheckedChanged += new System.EventHandler(this.RbReceiveAnyDeviceCheckedChanged);
			// 
			// lblDevicesDescription
			// 
			this.lblDevicesDescription.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
									| System.Windows.Forms.AnchorStyles.Left) 
									| System.Windows.Forms.AnchorStyles.Right)));
			this.lblDevicesDescription.Location = new System.Drawing.Point(0, 13);
			this.lblDevicesDescription.Name = "lblDevicesDescription";
			this.lblDevicesDescription.Size = new System.Drawing.Size(346, 16);
			this.lblDevicesDescription.TabIndex = 1;
			this.lblDevicesDescription.Text = "Select which devices\' notifications we\'ll care about";
			// 
			// lblDevices
			// 
			this.lblDevices.AutoSize = true;
			this.lblDevices.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.lblDevices.Location = new System.Drawing.Point(0, 0);
			this.lblDevices.Name = "lblDevices";
			this.lblDevices.Size = new System.Drawing.Size(53, 13);
			this.lblDevices.TabIndex = 1;
			this.lblDevices.Text = "Devices";
			// 
			// notifyIcon
			// 
			this.notifyIcon.BalloonTipIcon = System.Windows.Forms.ToolTipIcon.Info;
			this.notifyIcon.ContextMenuStrip = this.contextNotify;
			this.notifyIcon.Icon = ((System.Drawing.Icon)(resources.GetObject("notifyIcon.Icon")));
			this.notifyIcon.Text = "WinDroidNotifier";
			this.notifyIcon.Visible = true;
			this.notifyIcon.MouseClick += new System.Windows.Forms.MouseEventHandler(this.NotifyIconMouseClick);
			// 
			// contextNotify
			// 
			this.contextNotify.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
									this.preferencesToolStripMenuItem,
									this.exitToolStripMenuItem});
			this.contextNotify.Name = "contextNotify";
			this.contextNotify.ShowImageMargin = false;
			this.contextNotify.Size = new System.Drawing.Size(131, 48);
			// 
			// preferencesToolStripMenuItem
			// 
			this.preferencesToolStripMenuItem.Name = "preferencesToolStripMenuItem";
			this.preferencesToolStripMenuItem.Size = new System.Drawing.Size(130, 22);
			this.preferencesToolStripMenuItem.Text = "&Preferences...";
			this.preferencesToolStripMenuItem.Click += new System.EventHandler(this.PreferencesToolStripMenuItemClick);
			// 
			// exitToolStripMenuItem
			// 
			this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
			this.exitToolStripMenuItem.Size = new System.Drawing.Size(130, 22);
			this.exitToolStripMenuItem.Text = "&Exit";
			this.exitToolStripMenuItem.Click += new System.EventHandler(this.ExitToolStripMenuItemClick);
			// 
			// MainForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.BackColor = System.Drawing.SystemColors.Control;
			this.ClientSize = new System.Drawing.Size(382, 433);
			this.Controls.Add(this.groupNotifications);
			this.Controls.Add(this.groupGeneralOptions);
			this.Controls.Add(this.btnSampleNotification);
			this.Controls.Add(this.lblReceiveState);
			this.DoubleBuffered = true;
			this.ForeColor = System.Drawing.SystemColors.ControlText;
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
			this.MaximizeBox = false;
			this.MinimumSize = new System.Drawing.Size(390, 460);
			this.Name = "MainForm";
			this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
			this.Text = "WinDroidNotifier Preferences";
			this.SizeChanged += new System.EventHandler(this.MainFormSizeChanged);
			this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainFormFormClosing);
			this.Load += new System.EventHandler(this.MainFormLoad);
			this.groupGeneralOptions.ResumeLayout(false);
			this.groupGeneralOptions.PerformLayout();
			this.groupNotifications.ResumeLayout(false);
			this.panel3.ResumeLayout(false);
			this.panel3.PerformLayout();
			this.tableLayoutPanel1.ResumeLayout(false);
			this.tableLayoutPanel1.PerformLayout();
			this.panel2.ResumeLayout(false);
			this.panel2.PerformLayout();
			this.contextNotify.ResumeLayout(false);
			this.ResumeLayout(false);
			this.PerformLayout();
		}
		private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem preferencesToolStripMenuItem;
		private System.Windows.Forms.ContextMenuStrip contextNotify;
		private System.Windows.Forms.LinkLabel linkDownloadGrowl;
		private System.Windows.Forms.CheckBox chkUseGrowl;
		private System.Windows.Forms.Button btnSampleNotification;
		private System.Windows.Forms.NotifyIcon notifyIcon;
		private System.Windows.Forms.Label label5;
		private System.Windows.Forms.Label label4;
		private System.Windows.Forms.Label label9;
		private System.Windows.Forms.Label label8;
		private System.Windows.Forms.Label label7;
		private System.Windows.Forms.Label label6;
		private System.Windows.Forms.CheckBox checkBox5;
		private System.Windows.Forms.CheckBox checkBox4;
		private System.Windows.Forms.CheckBox checkBox2;
		private System.Windows.Forms.CheckBox checkBox1;
		private System.Windows.Forms.CheckBox checkBox3;
		private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Label lblNotificationActions;
		private System.Windows.Forms.Label lblNotificationActionsDescription;
		private System.Windows.Forms.Panel panel3;
		private System.Windows.Forms.Button btnAddDevice;
		private System.Windows.Forms.Button btnRemoveDevice;
		private System.Windows.Forms.ColumnHeader colDeviceName;
		private System.Windows.Forms.ColumnHeader colDeviceID;
		private System.Windows.Forms.ListView lvwReceiveDevice;
		private System.Windows.Forms.RadioButton rbReceiveAnyDevice;
		private System.Windows.Forms.RadioButton rbReceiveSpecifiedDevice;
		private System.Windows.Forms.Label lblDevices;
		private System.Windows.Forms.Label lblDevicesDescription;
		private System.Windows.Forms.Panel panel2;
		private System.Windows.Forms.GroupBox groupNotifications;
		private System.Windows.Forms.CheckBox chkStartAtLogin;
		private System.Windows.Forms.GroupBox groupGeneralOptions;
		private System.Windows.Forms.Label lblReceiveState;
		private System.Windows.Forms.Button btnStopReceive;
		private System.Windows.Forms.Button btnStartReceive;
	}
}
