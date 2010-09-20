using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Xml;
using System.Xml.Serialization;

namespace org.chieke.WinDroidNotifier
{
	/// <summary>
	/// Defines the layout for the preferences file
	/// </summary>
	[XmlRootAttribute(ElementName="DPLConfiguration", IsNullable=false)]
	public static class NotifierPreferences {
		public static Preferences preferences = new Preferences();
		private const string PREFERENCES_FILE = "preferences.xml";

		public static bool Load() {
			try {
				if (File.Exists(PREFERENCES_FILE)) {
					using (StreamReader sr = new StreamReader(PREFERENCES_FILE)) {
						string s = sr.ReadToEnd();
						NotifierPreferences.preferences = (Preferences)DeserializeObject(s);
						sr.Close();
					}
				} else {
					return false;
				}
			} catch {
				throw;
			}

			return true;
		}

		public static bool Save() {
			try {
				using (StreamWriter sw = new StreamWriter(PREFERENCES_FILE)) {
					sw.WriteLine(SerializeObject(NotifierPreferences.preferences));
					sw.Close();
				}
			} catch {
				throw;
			}

			return true;
		}

		private static string SerializeObject(object pObject) {
	        try {
	            string XmlizedString = null;
	            MemoryStream memoryStream = new MemoryStream();
	            XmlSerializer xs = new XmlSerializer(typeof(Preferences));
	            XmlTextWriter xmlTextWriter = new XmlTextWriter(memoryStream, Encoding.UTF8);

	            xs.Serialize(xmlTextWriter, pObject);
	            memoryStream = (MemoryStream)xmlTextWriter.BaseStream;
	            XmlizedString = UTF8ByteArrayToString(memoryStream.ToArray());
	            return XmlizedString;
			} catch {
				throw;
			}
	    }

		public static object DeserializeObject(string pXmlizedString) {
	        XmlSerializer xs = new XmlSerializer(typeof(Preferences));
	        MemoryStream memoryStream = new MemoryStream(StringToUTF8ByteArray(pXmlizedString));
	        XmlTextWriter xmlTextWriter = new XmlTextWriter(memoryStream, Encoding.UTF8);

	        return xs.Deserialize(memoryStream);
	    }

		private static string UTF8ByteArrayToString(byte[] characters) {
			UTF8Encoding encoding = new UTF8Encoding();
			string constructedString = encoding.GetString (characters);
		
			return (constructedString);
		}

		private static byte[] StringToUTF8ByteArray(string pXmlString) {
			UTF8Encoding encoding = new UTF8Encoding();
			byte[] byteArray = encoding.GetBytes(pXmlString);
		
			return byteArray;
		}
	}

	[Serializable]
	public class Preferences {
		public bool UseGrowl = false;
	}
}
