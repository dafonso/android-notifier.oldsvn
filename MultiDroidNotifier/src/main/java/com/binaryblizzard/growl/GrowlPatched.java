//
// $Id: Growl.java,v 1.1 2006/10/17 22:06:25 smartin Exp $
//

// Packages

package com.binaryblizzard.growl;

import com.binaryblizzard.util.Options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.HashMap;

/**
 * An object that can be used to send Growl notifications to a Mac. This is based on a python
 * script called netgrowl.py that I found at http://the.taoofmac.com/space/Projects/netgrowl.py.
 * The protocol information can be found at http://growl.info/documentation/developer/protocol
 *
 * @version $Revision: 1.1 $
 * @author Stephen Martin
 *
 * Copyright 2006 Stephen Martin, Binary Blizzard Software. All rights reserved. 
 * 
 * Changed to allow registration without having to send a notification and throw exception if could not send UDP datagrams.
 */

public class GrowlPatched implements Runnable {

    /** The Growl UDP Port. */

    private static final int GROWL_UDP_PORT = 9887;

    /** The growl protocol version. */

    private static final byte GROWL_PROTOCOL_VERSION = 1;

    /** The growl registration packet type. */

    private static final byte GROWL_REGISTRATION_PACKET = 0;

    /** The growl notification packet type. */

    private static final byte GROWL_NOTIFICATION_PACKET = 1;

    /** The time between sending registrations. */

    private static final int REGISTRATION_INTERVAL = 30000;

    /** A table of hosts and passwords that the notifications will be sent to. */

    private final HashMap<String, String> growlHosts = new HashMap<String, String>();


    /** The registrations of the notifications that this object will send. */

    private GrowlRegistrations growlRegistrations;

    /** The socket for sending and receiving  Growl messages. */

    private DatagramSocket growlSocket;

    /** The Growl listener. */

    private GrowlListener growlListener;

    /** The Thread used to receive growl messages. */

    private Thread growlThread;

    /** The password used for messages received by this object. */

    private String password;

    /** The time that the registratitons were last sent. */

    private long lastRegistration;

    /**
     * Create the growl object.
     *
     * @throws GrowlException If an error occurs creating the socket.
     */

    public GrowlPatched() throws GrowlException {

        try {
            growlSocket = new DatagramSocket();
        } catch (SocketException se) {
            throw new GrowlException(se);
        }
    }

    /**
     * Get the notification registrations.
     *
     * @param applicationName The name of this application for growl registration.
     * @return The registrations.
     */

    public synchronized GrowlRegistrations getRegistrations(String applicationName) {

        if (growlRegistrations == null)
            growlRegistrations = new GrowlRegistrations(applicationName);
        return growlRegistrations;
    }

    /**
     * Start listening for growl messages.
     *
     * @param growlListener The object to receive Growl notifications.
     * @param password The Growl network password, this may be null.
     * @throws GrowlException If there is an error creating the socket.
     */
    @SuppressWarnings("hiding")
    public synchronized void listenForGrowls(GrowlListener growlListener, String password) throws GrowlException {

        // Make sure that we are not already listening

        if (growlThread != null)
            return;

        // Replace the socket with one bound to the growl port

        try {
            growlSocket = new DatagramSocket(GROWL_UDP_PORT);
        } catch (SocketException se) {
            throw new GrowlException(se);
        }

        // Start a thread to listen for growl messages

        this.growlListener = growlListener;
        this.password = password;
        growlThread = new Thread(this, "Growl Thread");
        growlThread.start();
    }

    /**
     * Close the underlying socket.
     */

    public void close() {

        if (growlSocket != null)
            growlSocket.close();
    }

    /**
     * Add a growl host.
     *
     * @param host The name or IP address of the growl host.
     * @param password The password of the growl host.
     */
    @SuppressWarnings("hiding")
    public synchronized void addGrowlHost(String host, String password) {

        // Ignore duplicate hosts

        if (growlHosts.get(host) != null)
            return;

        // Register the host

        growlHosts.put(host, password);
    }

    /**
     * Send out the registration packets.
     *
     * @throws GrowlException If an error occurs.
     */

    public void sendRegistrations() throws GrowlException {

        byte[] packet = encodeRegistrationPacket();
        sendPacket(packet);
    }

    /**
     * Send a notification to the growl hosts.
     *
     * @param notification The notification to send.
     * @throws GrowlException If an error occurs.
     */

    public void sendNotification(GrowlNotification notification) throws GrowlException {

        // Check if it's time to send the registrations

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastRegistration) > REGISTRATION_INTERVAL) {
            sendRegistrations();
            lastRegistration = currentTime;
        }

        // Send the notification

        byte[] packet = encodeNotificationPacket(notification);
        sendPacket(packet);
    }

    /**
     * Encode a registration packet.
     *
     * @return An array of bytes that can be sent as a registration packet.
     * @throws GrowlException If the bytes can not be encoded.
     */

    private synchronized byte[] encodeRegistrationPacket() throws GrowlException {

        try {

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            // Add the growl protocol info and packet type

            bout.write(GROWL_PROTOCOL_VERSION);
            bout.write(GROWL_REGISTRATION_PACKET);

            // Add the length of the application name to the packet

            byte[] bytes = growlRegistrations.applicationName.getBytes("UTF-8");
            bout.write(bytes.length >>> 8);
            bout.write(bytes.length & 0xff);

            // Append the number of notifications and the number of defaults to the packet

            bout.write(growlRegistrations.registeredNotifications.size());
            bout.write(growlRegistrations.defaults.size());

            // Add the application name to the packet

            bout.write(bytes);

            // Write each of the notifications

            for (String notification : growlRegistrations.registeredNotifications) {

                // Write the notification

                bytes = notification.getBytes("UTF-8");
                bout.write(bytes.length >>> 8);
                bout.write(bytes.length & 0xff);
                bout.write(bytes);
            }

            // Write the defauts list

            for (Integer def : growlRegistrations.defaults)
                bout.write(def);

            // Return the encoded bytes

            return bout.toByteArray();

        } catch (IOException ioe) {
            throw new GrowlException(ioe);
        }
    }

    /**
     * Decode a GrowlRegistrations from a packet received.
     *
     * @param packet The packet to decode.
     * @return A decoded GrowlRegistrations or null if there was an error.
     */

    private GrowlRegistrations decodeRegistrationPacket(DatagramPacket packet) {

        byte[] bytes = packet.getData();

        // Collect the parameters of the registration

        int applicationNameLength = (bytes[2] << 8) | (bytes[3] & 0xff);
        int nall = bytes[4];
        int ndef = bytes[5];

        // Register the notifications

        GrowlRegistrations registrations = new GrowlRegistrations(new String(bytes, 6, applicationNameLength));
        int pos = 6 + applicationNameLength;
        for (int i = 0; i < ndef; i++) {
            int notificationLength = (bytes[pos] << 8) | (bytes[pos + 1] & 0xff);
            String notification = new String(bytes, pos + 2, notificationLength);
            registrations.registeredNotifications.add(notification);
            pos += notificationLength + 2;
        }

        // Enable them

        for (int i = 0; i < nall; i++) {
            int index = bytes[pos++];
            registrations.defaults.add(index);
        }

        return registrations;
    }

    /**
     * Encode a packet for a notification.
     *
     * @param notification The Notification to build the packet for.
     * @return The bytes for the packet.
     * @throws GrowlException If the bytes can not be encoded.
     */

    private byte[] encodeNotificationPacket(GrowlNotification notification) throws GrowlException {

        try {

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            // Add the growl protocol info and packet type

            bout.write(GROWL_PROTOCOL_VERSION);
            bout.write(GROWL_NOTIFICATION_PACKET);

            // Encode the flags

	    int flags = (short ) ((notification.priority & 0x07) * 2);
	    if (notification.priority < 0)
                flags |= 0x08;
            if (notification.sticky)
                flags |= 0x01;
            bout.write(flags >>> 8);
            bout.write(flags & 0xff);

            // Encode the lengths of the strings

            bout.write(notification.notification.length() >>> 8);
            bout.write(notification.notification.length() & 0xff);
            bout.write(notification.title.length()  >>> 8);
            bout.write(notification.title.length() & 0xff);
            bout.write(notification.description.length()  >>> 8);
            bout.write(notification.description.length() & 0xff);
            bout.write(notification.applicationName.length()  >>> 8);
            bout.write(notification.applicationName.length() & 0xff);

            // Encode the strings

            byte[] bytes = notification.notification.getBytes("UTF-8");
            bout.write(bytes);
            bytes = notification.title.getBytes("UTF-8");
            bout.write(bytes);
            bytes = notification.description.getBytes("UTF-8");
            bout.write(bytes);
            bytes = growlRegistrations.applicationName.getBytes("UTF-8");
            bout.write(bytes);

            return bout.toByteArray();

        } catch (IOException ioe) {
            throw new GrowlException(ioe);
        }
    }

    /**
     * Decode a GrowlNotication from a packet received.
     *
     * @param packet The packet to decode.
     * @return A decoded GrowlNotification or null if there was an error.
     */

    private GrowlNotification decodeNotificationPacket(DatagramPacket packet)  {

        byte[] bytes = packet.getData();

        // Decode the flags

        int flags = (bytes[2] << 8) | (bytes[3] & 0xff);
        boolean sticky = (flags & 0x01) != 0;
	int priority = (flags >>> 1) & 0x07;
        if ((flags & 0x08) != 0)
            priority = priority | 0xfffffff8;

	// Collect the lengths of the strings

        int notificationLength = (bytes[4] << 8) | (bytes[5] & 0xff);
        int titleLength = (bytes[6] << 8) | (bytes[7] & 0xff);
        int descriptionLength = (bytes[8] << 8) | (bytes[9] & 0xff);
        int applicationNameLength = (bytes[10] << 8) | (bytes[11] & 0xff);

        // Create the notifcation

        return new GrowlNotification(
                new String(bytes, 12, notificationLength),
                new String(bytes, 12 + notificationLength, titleLength),
                new String(bytes, 12 + notificationLength + titleLength, descriptionLength),
                new String(bytes, 12 + notificationLength + titleLength + descriptionLength, applicationNameLength),
                sticky, priority);
    }

    /**
     * Send a packet to all growl hosts.
     *
     * @param packet The packet to send
     */

    @SuppressWarnings("hiding")
    private void sendPacket(byte[] packet) throws GrowlException {

        for (String host : growlHosts.keySet())
            try {

                // Get the next host and password to send the packet to

                String password = growlHosts.get(host);

                // Compute the md5 checksum on the existing data

                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.reset();
                md5.update(packet);

                // Sign the bytes with the password

                if ((password != null) && !password.equals(""))
                    md5.update(password.getBytes("UTF-8"));

                // Append the checksum to the data

                byte[] payload = new byte[packet.length + 16];
                System.arraycopy(packet, 0, payload, 0, packet.length);
                System.arraycopy(md5.digest(), 0, payload, packet.length, 16);

                // Create a datagram socket to sent the packet

                DatagramPacket datagramPacket = new DatagramPacket(payload, payload.length, InetAddress.getByName(host), GROWL_UDP_PORT);
                growlSocket.send(datagramPacket);

            } catch (Exception ex) {
                throw new GrowlException(ex);
            }
    }

    /**
     * Receive growl messages and deliver them to the listener
     *
     * @see Runnable#run()
     */

    public void run() {

        // Create the packet to receive the data

        byte[] bytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        while (true)
            try {

                // Receive the packet

                growlSocket.receive(packet);
                if (packet.getLength() < 18)
                    throw new IOException("Packet is way too small");

                // Check the protocol

                if (bytes[0] != GROWL_PROTOCOL_VERSION)
                    throw new IOException("Unsupported Growl Protocol Version: " + bytes[0]);

                // Check the packet checksum

                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.reset();
                md5.update(bytes, 0, packet.getLength() - 16);
                if ((password != null) && ! password.equals(""))
                    md5.update(password.getBytes("UTF-8"));
                byte[] checksum = md5.digest();
                for (int i = 0; i < 16; i++)
                    if (checksum[i] != bytes[packet.getLength() - 16 + i])
                        throw new IOException("Checksum failed");

                // Check the packet type and process the packet

                switch (bytes[1]) {
                    case GROWL_REGISTRATION_PACKET:
                        GrowlRegistrations registrations = decodeRegistrationPacket(packet);
                        if ((registrations != null) && (growlListener != null))
                            growlListener.receiveRegistrations(registrations);
                        break;

                    case GROWL_NOTIFICATION_PACKET:
                        GrowlNotification notification = decodeNotificationPacket(packet);
                        if ((notification != null) && (growlListener != null))
                            growlListener.receiveNotification(notification);
                        break;

                    default:
                        throw new IOException("Unknown packet type: " + bytes[1]);
                }

            } catch (Throwable throwable) {

                // Check to make sure that the socket did not get closed

                if (growlSocket.isClosed())
                    return;

                // For now just dump the exception

                throwable.printStackTrace();
            }
}

    /**
     * Main for testing.
     *
     * usage: java com.binaryblizzard.growl.Growl [-p <password>] [<host>]
     *
     * if no arguments are given this listens for messages.
     *
     * @param args The command line arguments.
     */

    public static void main(String[] args) {

        try {

            // Process the command line options

            String applicationName = "Growl Test Program";
            Options options = new Options("Growl", applicationName);
            options.addOption("password", 'p', false, Options.ARGUMENT_REQUIRED, null, "The Growl network password");
            options.parseArgs(args);
            String host = options.getRemainingArgs().size() > 0 ? (String) options.getRemainingArgs().get(0) : null;

            // Create the Growl object

            GrowlPatched growl = new GrowlPatched();
            if (host != null) {

                // Send some growls to the target host

                growl.addGrowlHost(host, options.getOption("password"));
                GrowlRegistrations registrations = growl.getRegistrations(applicationName);
                registrations.registerNotification("test", true);
		growl.sendNotification(new GrowlNotification("test", "Test notification", "This is a test\nLow priority", applicationName, false, GrowlNotification.VERY_LOW));
		growl.sendNotification(new GrowlNotification("test", "Test notification", "This is a test\nModerate priority", applicationName, false, GrowlNotification.MODERATE));
		growl.sendNotification(new GrowlNotification("test", "Test notification", "This is a test\nNormal priority", applicationName, false, GrowlNotification.NORMAL));
		growl.sendNotification(new GrowlNotification("test", "Test notification", "This is a test\nHigh priority", applicationName, false, GrowlNotification.HIGH));
		growl.sendNotification(new GrowlNotification("test", "Test notification", "This is a test\nEmergency priority", applicationName, false, GrowlNotification.EMERGENCY));
		growl.sendNotification(new GrowlNotification("test", "Test notification", "This is a test\nSticky", applicationName, true, GrowlNotification.NORMAL));

            } else {

                // Listen for growls

                growl.listenForGrowls(new GrowlListener() {
                    public void receiveNotification(GrowlNotification notification) {
			System.out.println("Got notification " + notification);
                    }

                    public void receiveRegistrations(GrowlRegistrations registrations) {
			System.out.println("Got registrations " + registrations);
                    }
                }, options.getOption("password"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
