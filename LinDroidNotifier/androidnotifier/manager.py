#!/usr/bin/python2.4

"""Manager which receives and acts on notifications.

This manager will attach all listeners, receive notifications from them,
then dispatch the received notifications to actions.

It also handles preferences being changed about which actions to take on
which notifications and which methods to use.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

from actions.display import DisplayAction
from listeners.bluetooth import BluetoothListener
from listeners.wifi import WifiListener
from notification import Notification

_NUM_LAST_NOTIFICATIONS = 20

class NotificationManager:

    def __init__(self, preferences):
        self.preferences = preferences
        self.listeners = [
            WifiListener(),
            BluetoothListener()
            ]
        self.actions= [
            DisplayAction()
            ]
        self.connections = {}
        self.last_notification_ids = []

        preferences.connect('preference-change', self.on_preferences_changed)

    def start(self):
        for listener in self.listeners:
            self._start_listener(listener)

    def stop(self):
        for listener in self.listeners:
            self._stop_listener(listener)

    def on_notification(self, raw_data):
        notification = Notification(raw_data)

        if self._is_duplicate_notification(notification):
            return

        for action in self.actions:
            if self._is_action_enabled_for_type(action, notification):
                action.handle_notification(notification)

    def on_preferences_changed(self):
        for listener in self.listeners:
            enabled = self._is_listener_enabled(listener)
            started = self._is_listener_started(listener)
            if enabled and not started:
                _start_listener(listener)
            if not enabled and started:
                _stop_listener(listener)

    def _start_listener(self, listener):
        if listener in self.connections:
            return

        self.connections[listener] = \
            listener.connect('android-notify', self.on_notification)
        listener.start()

    def _stop_listener(self, listener):
        if not listener in self.connections:
            return

        listener.disconnect(self.connections[listener])
        listener.stop()

    def _is_listener_started(self, listener):
        return listener in self.connections

    def _is_duplicate_notification(self, notification):
        id = notification.notification_id
        if id in self.last_notification_ids:
            return True
        self.last_notification_ids.append(id)
        if len(self.last_notification_ids) > _NUM_LAST_NOTIFICATIONS:
            del self.last_notification_ids[0]

    def _is_listener_enabled(self, listener_name):
        # TODO
        return True

    def _is_action_enabled_for_type(self, action, notification):
        # TODO
        return True
