
using System;
using System.Threading;
using System.Windows.Forms;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of Program.
	/// </summary>
	public class Program
	{
		public Program()
		{
		}

		[STAThread]
		public static void Main(string[] args) {
			Application.EnableVisualStyles();
			Application.SetCompatibleTextRenderingDefault(false);
			Application.ThreadException += new ThreadExceptionEventHandler(OnThreadException);
			Application.Run(new MainForm());
		}

		private static void OnThreadException(object sender, ThreadExceptionEventArgs e) {
			Thread t = (Thread)sender;
			Exception threadexception = e.Exception;
			ExceptionForm ef = new ExceptionForm(threadexception);
			ef.Show();
		}
	}
}
