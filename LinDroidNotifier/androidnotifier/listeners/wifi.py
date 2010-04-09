#!/usr/bin/python

"""Wifi listener.

This module listens on UDP port 10600 and receives notifications through it.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

from gobject import GObject
import gobject
import select
import SocketServer
from threading import Thread
import threading

PORT = 10600

class _WifiHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        data = self.request[0].strip()
        if data != 'quit':
            self.server._wifi_listener.emit('android-notify', data)


class WifiListener(GObject):

    def __init__(self):
        GObject.__init__(self)

    def start(self):
        if hasattr(self, 'server'):
            raise 'Listener already started'

        self.server = SocketServer.UDPServer(('', PORT), _WifiHandler)
        self.server._wifi_listener = self
        self.server_thread = Thread(target=self.server.serve_forever)
        self.server_thread.setDaemon(True)
        self.server_thread.start()

    def stop(self):
        if hasattr(self, 'server'):
            if hasattr(self.server, 'shutdown'):
                self.server.shutdown()
                del self.server
            else:
                # TODO: Stop listening on python < 2.6
                pass


gobject.type_register(WifiListener)
gobject.signal_new('android-notify', WifiListener,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, (str,))
