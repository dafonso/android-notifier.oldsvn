

using System;
using System.Drawing;
using System.Windows.Forms;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of ExceptionForm.
	/// </summary>
	public partial class ExceptionForm : Form
	{
		private Exception mException = null;

		public ExceptionForm(Exception e) {
			//
			// The InitializeComponent() call is required for Windows Forms designer support.
			//
			InitializeComponent();

			mException = e;
		}

		private void ExceptionFormLoad(object sender, EventArgs e) {
			lblExceptionMessage.Text = String.Empty;

			if (this.mException != null) {
				lblExceptionMessage.Text = mException.Message;
				txtStackTrace.Text = mException.Message + Environment.NewLine + Environment.NewLine + mException.StackTrace;
			}
		}

		private void BtnCloseClick(object sender, EventArgs e) {
			this.Close();
		}
	}
}
