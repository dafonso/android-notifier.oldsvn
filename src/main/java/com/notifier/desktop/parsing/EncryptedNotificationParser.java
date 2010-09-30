/*
 * Android Notifier Desktop is a multiplatform remote notification client for Android devices.
 *
 * Copyright (C) 2010  Leandro Aparecido
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.notifier.desktop.parsing;

import java.security.*;

import javax.crypto.*;

import org.slf4j.*;

import com.notifier.desktop.*;
import com.notifier.desktop.util.*;

public abstract class EncryptedNotificationParser implements NotificationParser<byte[]> {

	private static final Logger logger = LoggerFactory.getLogger(EncryptedNotificationParser.class);

	private boolean decrypt;
	private Encryption encryption;

	public EncryptedNotificationParser(ApplicationPreferences preferences) {
		setEncryption(preferences.isEncryptCommunication(), preferences.getCommunicationPassword());
	}

	@Override
	public void setEncryption(boolean decrypt, byte[] key) {
		this.decrypt = decrypt;
		if (key.length > 0) {
			encryption = new Encryption(key);
		} else {
			encryption = null;
		}
	}

	protected byte[] decryptIfNecessary(byte[] msg) {
		if (decrypt) {
			if (encryption == null) {
				logger.debug("Decryption enabled but no password set, ignoring notification");
				return null;
			}
			try {
				return encryption.decrypt(msg);
			} catch (GeneralSecurityException e) {
				if (e instanceof IllegalBlockSizeException) { // Message is not encrypted
					logger.debug("Got notification not encrypted but set to decrypt, ignoring");
					return null;
				} else {
					logger.debug("Got notification but could not decrypt it, ignoring");
					return null;
				}
			}
		}
		return msg;
	}
}
