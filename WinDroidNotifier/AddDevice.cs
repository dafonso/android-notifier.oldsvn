using System;
using System.Drawing;
using System.Windows.Forms;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of AddDevice.
	/// </summary>
	public partial class AddDevice : Form
	{
		public AddDevice()
		{
			//
			// The InitializeComponent() call is required for Windows Forms designer support.
			//
			InitializeComponent();
			
			//
			// TODO: Add constructor code after the InitializeComponent() call.
			//
		}

		void BtnOKClick(object sender, EventArgs e) {
			this.DialogResult = DialogResult.OK;
			this.Close();
		}

		void BtnCancelClick(object sender, EventArgs e) {
			this.DialogResult = DialogResult.Cancel;
			this.Close();
		}

		public string DeviceID {
			get { return this.txtDeviceID.Text; }
			set { this.txtDeviceID.Text = value; }
		}

		public string DeviceName {
			get { return this.txtDeviceName.Text; }
			set { this.txtDeviceName.Text = value; }
		}
	}
}
