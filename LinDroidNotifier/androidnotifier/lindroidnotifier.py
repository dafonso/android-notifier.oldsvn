#!/usr/bin/python

"""Entry point for the linux android notifier.

This module starts up the application.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

import gtk, gtk.glade
import preferences
from manager import NotificationManager

# TODO: Detect the data path (for when this is installed somewhere)
gladefile = gtk.glade.XML('../data/lindroidnotifier.glade')

# TODO: Show the gnome menu icon (applet), open prefs from there
prefs = preferences.Preferences()
prefs_dialog = preferences.PreferencesDialog(gladefile)
prefs_dialog.show()

manager = NotificationManager(prefs)
manager.start()

# TODO: Remove this test notification once there are listeners
manager._on_notification('1234/5678/RING/Mom is calling')

# Article about threading in GTK:
# <http://unpythonic.blogspot.com/2007/08/using-threads-in-pygtk.html>
gtk.gdk.threads_init()

gtk.main()

manager.stop()
