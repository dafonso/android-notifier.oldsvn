
using System;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of EventType.
	/// </summary>
	public class Enums
	{
		public Enums()
		{
		}
		
		public enum EventType {
			Unknown,
			RING,
			SMS,
			MMS,
			BATTERY,
			PING
		};
	}
}
