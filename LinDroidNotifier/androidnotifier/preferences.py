"""Preferences manager and UI.

This module manages preferences for the notifier application, and controls
the preference management UI.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

import gtk, gtk.glade
from gobject import GObject
import gobject
import os
from pprint import pprint

class Preferences(GObject):
    DEFAULTS = {
        'startAtLogin': False,
        'bluetoothMethod': False,
        'wifiMethod': False,
        'usbMethod': False,
    }

    @classmethod
    def type_of(cls, key):
        return type(cls.DEFAULTS[key])

    def __init__(self, filename):
        GObject.__init__(self)
        self.filename = filename
        self._keyvalues = dict(self.DEFAULTS)
        if not os.path.exists(self.filename):
            self.save()

    def __getitem__(self, key):
        return self._keyvalues[key]

    def __setitem__(self, key, value):
        self._keyvalues[key] = value

    def __iter__(self):
        return self._keyvalues.iterkeys()

    def save(self):
        with open(self.filename, 'w') as f:
            pprint(self._keyvalues, f)

    def load(self):
        with open(self.filename, 'r') as f:
            self._keyvalues.update(eval(f.read()))

gobject.signal_new('preference-change', Preferences,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, ())
gobject.type_register(Preferences)

class PreferencesDialog:
    def __init__(self, gladefile, prefs):
        self.dialog = gladefile.get_widget('preferencesDialog')
        self.prefs = prefs
        self.widgets = {}
        for key in self.prefs:
            self.widgets[key] = gladefile.get_widget(key)
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
        gtk.main_quit() # XXX

    def on_cancel_clicked(self, *args):
        self.dialog.hide()
        gtk.main_quit() # XXX

    def _load_preferences(self):
        self.prefs.load()
        for key in self.prefs:
            t = self.prefs.type_of(key)
            w = self.widgets[key]
            if t is bool:
                w.set_active(self.prefs[key])
            else:
                raise RuntimeError, "can't persist prefs of type %s" % t

    def _save_preferences(self):
        for key in self.prefs:
            t = self.prefs.type_of(key)
            w = self.widgets[key]
            if t is bool:
                self.prefs[key] = w.get_active()
            else:
                raise RuntimeError, "can't persist prefs of type %s" % t
        self.prefs.save()
