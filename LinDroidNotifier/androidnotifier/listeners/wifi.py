#!/usr/bin/python

"""Wifi listener.

This module listens on UDP port 10600 and receives notifications through it.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

from gobject import GObject
import gobject
import SocketServer
from threading import Thread


class _WifiHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        data = self.request[0].strip()
        self.server.wifi_listener.emit('android-notify', data)


class WifiListener(GObject):
    PORT = 10600

    def __init__(self):
        GObject.__init__(self)

    def start(self):
        if hasattr(self, '_server'):
            print 'Listener already started'
            return

        self._server = SocketServer.UDPServer(('', self.PORT), _WifiHandler)
        self._server.wifi_listener = self
        self._server_thread = Thread(target=self._server.serve_forever)
        self._server_thread.setDaemon(True)
        self._server_thread.start()

    def stop(self):
        if hasattr(self, '_server'):
            if hasattr(self._server, 'shutdown'):
                self._server.shutdown()
                del self._server
            else:
                # The thread is in daemon mode, let it die
                # TODO: Fix the case when the app is not exiting
                pass


gobject.type_register(WifiListener)
gobject.signal_new('android-notify', WifiListener,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, (str,))
