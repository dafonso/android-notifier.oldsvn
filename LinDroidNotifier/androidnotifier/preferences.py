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

    def update(self, prefs):
        self._keyvalues.update(prefs)
        self.emit('preference-change')

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
    def __init__(self, gladefile, prefs, notification_manager):
        self.gladefile = gladefile
        self.dialog = gladefile.get_widget('preferencesDialog')
        self.prefs = prefs
        self.notification_manager = notification_manager

        handlers = {
            'on_prefs_okButton_clicked': self._on_ok_clicked,
            'on_prefs_cancelButton_clicked': self._on_cancel_clicked,
            'on_addDevice_clicked': self._on_addDevice_clicked,
            'on_removeDevice_clicked': self._on_removeDevice_clicked,
            'on_allDevices_clicked': lambda button: self._update_devices_sensitive(),
            'on_onlyPaired_clicked': lambda button: self._update_devices_sensitive() }
        gladefile.signal_autoconnect(handlers)

        self.devices_tree = gladefile.get_widget('pairedDevices')
        self.devices_store = gtk.ListStore(str)
        self.devices_tree.set_model(self.devices_store)
        self.devices_tree.append_column(
            gtk.TreeViewColumn(None, gtk.CellRendererText(), text=0))
        self.devices_tree.set_headers_visible(False)

    def _get_widget(self, name):
        return self.gladefile.get_widget(name)
    
    def _on_addDevice_clicked(self, button):
        dialog = gtk.MessageDialog(buttons=gtk.BUTTONS_CANCEL,
            message_format="Send a test notification from your device to add it to the list of approved devices.")
        dialog.connect('response', lambda sender, response_id: dialog.destroy())

        def on_notification(sender, notification):
            if notification.type == 'PING':
                already_present = [False]
                def visitor(model, path, iter, user_data):
                    if model.get(model.get_iter(path), 0)[0] == notification.device_id:
                        already_present[0] = True
                self.devices_store.foreach(visitor, None)
                if not already_present[0]:
                    self.devices_store.append([notification.device_id])
                dialog.destroy()
                return True

        handler_id = self.notification_manager.connect('android-notify', on_notification)
        dialog.run()
        self.notification_manager.disconnect(handler_id)

    def _on_removeDevice_clicked(self, button):
        selected_path = self.devices_tree.get_cursor()[0]
        if selected_path is not None:
            self.devices_store.remove(self.devices_store.get_iter(selected_path))

    def show(self):
        if not self.dialog.get_property('visible'):
            self._load_preferences()
            self.dialog.show()

    def _on_ok_clicked(self, *args):
        self._save_preferences()
        self.dialog.hide()

    def _on_cancel_clicked(self, *args):
        self.dialog.hide()

    def _update_devices_sensitive(self):
        # Update whether the list of devices to receive notifications from should
        # be 'sensitive' or enabled.
        enable = self._get_widget('onlyPaired').get_active()
        self.devices_tree.set_sensitive(enable)
        self._get_widget('addDevice').set_sensitive(enable)
        self._get_widget('removeDevice').set_sensitive(enable)

    def _load_preferences(self):
        self.prefs.load()

        for key in self.prefs.get_boolean_prefs():
            self._get_widget(key).set_active(self.prefs[key])
        
        self._get_widget('allDevices').set_active(
          self.prefs['receiveNotificationsFrom'] == 'any')
        self._get_widget('onlyPaired').set_active(
          self.prefs['receiveNotificationsFrom'] == 'these')
        self._update_devices_sensitive()

        if self.prefs['executeTarget'] is not None:
            self._get_widget('executeTargetChooser').set_filename(self.prefs['executeTarget'])

        for device in self.prefs['pairedDevices']:
            self.devices_store.append([device])

    def _save_preferences(self):
        new_prefs = dict()
        for key in self.prefs.get_boolean_prefs():
            new_prefs[key] = self._get_widget(key).get_active()

        if self._get_widget('allDevices').get_active():
            new_prefs['receiveNotificationsFrom'] = 'any'
        elif self._get_widget('onlyPaired').get_active():
            new_prefs['receiveNotificationsFrom'] = 'these'

        new_prefs['executeTarget'] = \
          self._get_widget('executeTargetChooser').get_filename()

        new_prefs['pairedDevices'] = list()
        iter = self.devices_store.get_iter_first()
        while iter != None:
            device = self.devices_store.get(iter, 0)[0]
            new_prefs['pairedDevices'].append(device)
            iter = self.devices_store.iter_next(iter)

        self.prefs.update(new_prefs)
        self.prefs.save()
