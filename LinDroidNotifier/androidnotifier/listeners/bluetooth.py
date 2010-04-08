#!/usr/bin/python2.4

"""Bluetooth listener.

This listener listens on and receives notifications with bluetooth RFCOMM
sockets.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

from gobject import GObject
import gobject


# TODO: Implement

class BluetoothListener(GObject):

    def __init__(self):
        GObject.__init__(self)

    def start(self):
        pass

    def stop(self):
        pass

gobject.type_register(BluetoothListener)
gobject.signal_new('android-notify', BluetoothListener,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, (str,))
