#!/usr/bin/python2.4

"""Preferences manager and UI.

This module manages preferences for the notifier application, and controls
the preference management UI.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

import gtk, gtk.glade
from gobject import GObject
import gobject

class Preferences(GObject):

    def __init__(self):
        GObject.__init__(self)

gobject.signal_new('preference-change', Preferences,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, ())
gobject.type_register(Preferences)

class PreferencesDialog:
    def __init__(self, gladefile):
        self.dialog = gladefile.get_widget('preferencesDialog')
        handlers = {
            'on_prefs_okButton_clicked': self.on_ok_clicked,
            'on_prefs_cancelButton_clicked': self.on_cancel_clicked }
        gladefile.signal_autoconnect(handlers)

    def show(self):
        self._load_preferences()
        self.dialog.show()

    def on_ok_clicked(self, *args):
        self._save_preferences()
        self.dialog.hide()

    def on_cancel_clicked(self, *args):
        self.dialog.hide()

    def _load_preferences(self):
        pass

    def _save_preferences(self):
        pass
