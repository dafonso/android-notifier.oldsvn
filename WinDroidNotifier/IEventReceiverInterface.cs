

using System;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of EventReceiverInterface.
	/// </summary>
	public interface IEventReceiverInterface
	{
		void Start();
		void Stop();
		event EventHandler OnNotificationReceived;
	}
}
