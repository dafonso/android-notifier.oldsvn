#!/usr/bin/python

"""Bluetooth listener.

This listener listens on and receives notifications with bluetooth RFCOMM
sockets.
"""

__author__ = 'wcauchois@gmail.com (William Cauchois)'

import bluetooth
import gtk
from gobject import GObject
import gobject
from threading import Thread

class BluetoothListener(GObject):
    PORT = 1 # The Bluetooth port for the server to listen on
    SERVICE_ID = '7674047e-6e47-4bf0-831f-209e3f9dd23f'
    SERVICE_CLASSES = [ SERVICE_ID ]
    SERVICE_NAME = 'AndroidNotifierService'
    # The timeout in seconds for the RFCOMM socket. This affects how
    # frequently the listener thread can monitor the stopping event.
    SOCK_TIMEOUT = 0.5
    BUFFER_SIZE = 1024

    def __init__(self):
        GObject.__init__(self)

        # Stop the thread using an event; from
        # <http://stackoverflow.com/questions/323972/is-there-any-way-
        # to-kill-a-thread-in-python>
        self._stop = False

    def _publish_service(self):
        self._sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self._sock.bind(('', self.PORT))
        self._sock.listen(bluetooth.PORT_ANY)
        bluetooth.advertise_service(self._sock, self.SERVICE_NAME,
                                    self.SERVICE_ID, self.SERVICE_CLASSES)

    def _unpublish_service(self):
        self._sock.close()

    def _run(self):
        self._sock.settimeout(self.SOCK_TIMEOUT)
        while not self._stop:
            try:
                client_sock, _ = self._sock.accept()
            except bluetooth.BluetoothError:
                continue # The socket timed out
            data = buffer = client_sock.recv(self.BUFFER_SIZE)
            while len(buffer) == BUFFER_SIZE:
                buffer = client_sock.recv(BUFFER_SIZE)
                if len(buffer) == 0:
                    break
                data += buffer
            self.emit('android-notify', data)

    def start(self):
        if hasattr(self, '_server_thread'):
            print 'Listener already started'
            return

        self._publish_service()
        self._server_thread = Thread(target=self._run)
        self._server_thread.start()

    def stop(self):
        if not hasattr(self, '_server_thread'):
            return

        self._stop = True
        self._unpublish_service()
        self._server_thread.join()
        del self._server_thread


gobject.type_register(BluetoothListener)
gobject.signal_new('android-notify', BluetoothListener,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, (str,))
