using System;
using org.chieke.WinDroidNotifier;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of NotificationArgs.
	/// </summary>
	public class NotificationArgs : EventArgs {
		public string DeviceId;
		public Enums.EventType NotificationType;
		public string EventContents;
		public string CompleteNotification;
	}
}
