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
    """
    Encapsulates a set of preferences, mapping preference names to their
    associated (variant) values. This object is backed by a local file;
    use save() and load() to synchronize changes to disc.
    """

    # List of preferences and their default values. The preference name should
    # (in most cases) correspond to its associated widget in the Glade-based UI.
    DEFAULTS = {
        'startAtLogin': False,
        'bluetoothMethod': False,
        'wifiMethod': False,
        'usbMethod': False,

        'ring.display': False,
        'ring.mute': False,
        'ring.execute': False,
        'ring.copy': False,

        'sms.display': False,
        'sms.mute': False,
        'sms.execute': False,
        'sms.copy': False,

        'mms.display': False,
        'mms.mute': False,
        'mms.execute': False,
        'mms.copy': False,

        'battery.display': False,
        'battery.mute': False,
        'battery.execute': False,
        'battery.copy': False,

        'executeTarget': None,

        'receiveNotificationsFrom': 'any', # Either 'any' or 'these'.

        'pairedDevices': [],
    }

    @classmethod
    def get_boolean_prefs(cls):
        return [pref for pref in cls.DEFAULTS if cls.type_of(pref) is bool]

    @classmethod
    def type_of(cls, pref):
        """
        Preferences.type_of(pref) -> type

        Returns the type of the specified preference.
        """
        return type(cls.DEFAULTS[pref])

    def __init__(self, filename):
        GObject.__init__(self)
        self.filename = filename
        self._keyvalues = dict(self.DEFAULTS)
        if not os.path.exists(self.filename):
            self.save()

    def __getitem__(self, key):
        return self._keyvalues.get(key)

    def __setitem__(self, key, value):
        if self._keyvalues[key] != value:
            self._keyvalues[key] = value
            self.emit('preference-change')

    def __iter__(self):
        return self._keyvalues.iterkeys()

    def save(self):
        with open(self.filename, 'w') as f:
            pprint(self._keyvalues, f)

    def load(self):
        self._keyvalues.clear()
        self._keyvalues.update(self.DEFAULTS)
        with open(self.filename, 'r') as f:
            self._keyvalues.update(eval(f.read()))
        self.emit('preference-change')

gobject.signal_new('preference-change', Preferences,
    gobject.SIGNAL_RUN_LAST, gobject.TYPE_NONE, ())
gobject.type_register(Preferences)

class PreferencesDialog:
    def __init__(self, gladefile, prefs):
        G = self.gladefile = gladefile
        self.dialog = G.get_widget('preferencesDialog')
        self.prefs = prefs
        self.widgets = {}
        for key in self.prefs:
            self.widgets[key] = G.get_widget(key)
        handlers = {
            'on_prefs_okButton_clicked': self.on_ok_clicked,
            'on_prefs_cancelButton_clicked': self.on_cancel_clicked,
            'on_addDevice_clicked': self.on_addDevice_clicked,
            'on_removeDevice_clicked': self.on_removeDevice_clicked,
            'on_allDevices_clicked': lambda button: self._update_pairedDevices(),
            'on_onlyPaired_clicked': lambda button: self._update_pairedDevices() }
        G.signal_autoconnect(handlers)

        self.devices_tree = G.get_widget('pairedDevices')
        self.devices_store = gtk.ListStore(str)
        self.devices_cellr = gtk.CellRendererText()

        self.devices_tree.set_model(self.devices_store)
        self.devices_tree.append_column(
          gtk.TreeViewColumn(None, self.devices_cellr, text=0))
        self.devices_tree.set_headers_visible(False)
    
    def on_addDevice_clicked(self, button):
        self.devices_cellr.set_property('editable', True)

        handler_id = None
        def on_edited(cell, path, new_text, model):
            cell.set_property('editable', False)
            # Is the device that the user wants to enter already in the devices
            # list? If so, don't add it. This variable has to be boxed in a list
            # because Python gets closures wrong.
            already_present = [False]
            def visitor(model, path, iter, user_data):
                #print model.get(model.get_iter(path), 0)[0]
                if model.get(model.get_iter(path), 0)[0] == new_text:
                    already_present[0] = True
            model.foreach(visitor, None)
            if not already_present[0] and len(new_text.strip()) > 0:
                model.set(model.get_iter(path), 0, new_text)
            else:
                model.remove(model.get_iter(path))
            cell.disconnect(handler_id)
        handler_id = self.devices_cellr.connect(
          'edited', on_edited, self.devices_store)

        entry_path = self.devices_store.get_path(self.devices_store.append(['']))
        self.devices_tree.set_cursor(entry_path, self.devices_tree.get_column(0), True)

    def on_removeDevice_clicked(self, button):
        selected_path = self.devices_tree.get_cursor()[0]
        if selected_path is not None:
            self.devices_store.remove(self.devices_store.get_iter(selected_path))

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

    def _update_pairedDevices(self):
        enable = self.gladefile.get_widget('onlyPaired').get_active()
        self.devices_tree.set_sensitive(enable)
        self.gladefile.get_widget('addDevice').set_sensitive(enable)
        self.gladefile.get_widget('removeDevice').set_sensitive(enable)

    def _load_preferences(self):
        self.prefs.load()

        G = self.gladefile

        for key in self.prefs.get_boolean_prefs():
            G.get_widget(key).set_active(self.prefs[key])
        
        G.get_widget('allDevices').set_active(
          self.prefs['receiveNotificationsFrom'] == 'any')
        G.get_widget('onlyPaired').set_active(
          self.prefs['receiveNotificationsFrom'] == 'these')
        self._update_pairedDevices()

        if self.prefs['executeTarget'] is not None:
            G.get_widget('executeTargetChooser').set_filename(
              self.prefs['executeTarget'])

        for device in self.prefs['pairedDevices']:
            self.devices_store.append([device])

    def _save_preferences(self):
        G = self.gladefile

        for key in self.prefs.get_boolean_prefs():
            self.prefs[key] = G.get_widget(key).get_active()

        if G.get_widget('allDevices').get_active():
            self.prefs['receiveNotificationsFrom'] = 'any'
        elif G.get_widget('onlyPaired').get_active():
            self.prefs['receiveNotificationsFrom'] = 'these'

        self.prefs['executeTarget'] = \
          G.get_widget('executeTargetChooser').get_filename()

        self.prefs['pairedDevices'] = list()
        iter = self.devices_store.get_iter_first()
        while iter != None:
            device = self.devices_store.get(iter, 0)[0]
            self.prefs['pairedDevices'].append(device)
            iter = self.devices_store.iter_next(iter)

        self.prefs.save()
