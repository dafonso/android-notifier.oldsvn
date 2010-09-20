
using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using org.chieke.WinDroidNotifier;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Description of UDPReceiver.
	/// </summary>
	public class UDPReceiver  : IEventReceiverInterface {
		private UdpClient sock = null;
		private IPEndPoint iep = null;
		private UdpState s = null;
		private static bool messageReceived = false;

		public UDPReceiver() {
		}

		public void Start() {
			sock = new UdpClient(10600);
			sock.JoinMulticastGroup(IPAddress.Parse("192.168.6.11"), 50);
			iep = new IPEndPoint(IPAddress.Any, 0);

			s = new UdpState();
			s.e = iep;
			s.u = sock;

			sock.BeginReceive(new AsyncCallback(ReceiveCallback), s);

			while (!messageReceived) {
				System.Threading.Thread.Sleep(100);
			}
		}

		public void Stop() {
			if (sock != null) {
				sock.Close();
				sock = null;
			}
		}

		// Create an event from interface event
		private event EventHandler NotificationEvent;
		event EventHandler IEventReceiverInterface.OnNotificationReceived {
			add {
				if (NotificationEvent != null) {
					lock (NotificationEvent) {
						NotificationEvent += value;
					}
				} else {
					NotificationEvent = new EventHandler(value);
				}
			}

			remove {
				if (NotificationEvent != null) {
					lock (NotificationEvent) {
						NotificationEvent -= value;
					}
				}
			}
		}

		public void ReceiveCallback(IAsyncResult ar) {
			try {
				UdpClient u = (UdpClient)((UdpState)(ar.AsyncState)).u;
				if (u != null) {
					IPEndPoint e = (IPEndPoint)((UdpState)(ar.AsyncState)).e;

					Byte[] receiveBytes = u.EndReceive(ar, ref e);
					string dataString = Encoding.ASCII.GetString(receiveBytes);

					this.HandleMessage(dataString);
					//messageReceived = true;
				}
			} catch {
				//
			}
		}

		private void HandleMessage(string msg) {
			EventHandler handler = NotificationEvent;
			if (handler != null) {
				string[] packageItems = msg.Split("/".ToCharArray());
				if (packageItems.Length >= 4) {
					string deviceId = packageItems[0];
					string notificationId = packageItems[1];
					string eventType = packageItems[2];
					string eventContents = packageItems[3];

					// event contents contains slashes
					// let's get them back
					if (packageItems.Length > 4) {
						string ec = String.Empty;
						for (int i = 3; i < packageItems.Length; i++) {
							ec += packageItems[i] + "/";
						}
						ec = ec.Substring(0, ec.Length - 1);
					}

					NotificationArgs notArgs = new NotificationArgs();
					notArgs.CompleteNotification = msg;
					notArgs.DeviceId = deviceId;
					notArgs.EventContents = eventContents;
					switch (eventType) {
						case "BATTERY":
							notArgs.NotificationType = Enums.EventType.BATTERY;
							break;
						case "MMS":
							notArgs.NotificationType = Enums.EventType.MMS;
							break;
						case "PING":
							notArgs.NotificationType = Enums.EventType.PING;
							break;
						case "RING":
							notArgs.NotificationType = Enums.EventType.RING;
							break;
						case "SMS":
							notArgs.NotificationType = Enums.EventType.SMS;
							break;
						default:
							notArgs.NotificationType = Enums.EventType.Unknown;
							break;
					}
					handler(this, notArgs);
				}
			}

			sock.BeginReceive(new AsyncCallback(ReceiveCallback), s);
		}
	}
}
