#!/usr/bin/python2.4

"""Wifi listener.

This module listens on UDP port 10600 and receives notifications through it.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

from gobject import GObject
import gobject

# TODO: Implement

class WifiListener(GObject):

    def __init__(self):
        GObject.__init__(self)

    def start(self):
        pass

    def stop(self):
        pass

gobject.type_register(WifiListener)
gobject.signal_new('android-notify', WifiListener,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, (str,))
