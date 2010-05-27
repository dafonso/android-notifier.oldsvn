"""Manager which receives and acts on notifications.

This manager will attach all listeners, receive notifications from them,
then dispatch the received notifications to actions.

It also handles preferences being changed about which actions to take on
which notifications and which methods to use.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

from actions.display import DisplayAction
from actions.copy import CopyAction
from listeners.rfcomm import BluetoothListener
from listeners.wifi import WifiListener
from notification import Notification

_NUM_LAST_NOTIFICATIONS = 20

class NotificationManager:

    def __init__(self, preferences):
        self._preferences = preferences
        self._listeners = [
            WifiListener(),
            BluetoothListener()
            ]
        self._actions= [
            DisplayAction(),
            CopyAction()
            ]
        self._connections = {}
        self._last_notification_ids = []

        preferences.connect('preference-change', self._on_preferences_changed)

    def start(self):
        for listener in self._listeners:
            if self._is_listener_enabled(listener):
                self._start_listener(listener)

    def stop(self):
        for listener in self._listeners:
            self._stop_listener(listener)

    def _on_notification(self, sender, raw_data):
        notification = Notification(raw_data)

        if self._is_duplicate_notification(notification):
            return

        for action in self._actions:
            if self._is_action_enabled_for_type(action, notification):
                action.handle_notification(notification)

    def _on_preferences_changed(self, sender):
        for listener in self._listeners:
            enabled = self._is_listener_enabled(listener)
            started = self._is_listener_started(listener)
            self._stop_listener(listener)
            self._start_listener(listener)
            if not enabled and started:
                self._stop_listener(listener)
            if enabled and not started:
                self._start_listener(listener)

    def _start_listener(self, listener):
        if listener in self._connections:
            return

        self._connections[listener] = \
            listener.connect('android-notify', self._on_notification)
        listener.start()

    def _stop_listener(self, listener):
        if not listener in self._connections:
            return

        listener.disconnect(self._connections[listener])
        listener.stop()

    def _is_listener_started(self, listener):
        return listener in self._connections

    def _is_duplicate_notification(self, notification):
        id = notification.notification_id
        if id in self._last_notification_ids:
            return True
        self._last_notification_ids.append(id)
        if len(self._last_notification_ids) > _NUM_LAST_NOTIFICATIONS:
            del self._last_notification_ids[0]

    def _is_listener_enabled(self, listener):
        if isinstance(listener, BluetoothListener):
            return self._preferences['bluetoothMethod']
        elif isinstance(listener, WifiListener):
            return self._preferences['wifiMethod']

    def _is_action_enabled_for_type(self, action, notification):
        if notification.type == 'PING':
            # Just display ping notifications; there aren't any associated
            # preferences.
            return action.name == 'display'
        pref_key = '%s.%s' % (notification.type.lower(), action.name)
        return self._preferences[pref_key] is True
