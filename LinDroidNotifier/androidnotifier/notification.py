"""Data object which holds a notification.

This object parses a notification from its serialized format, and provides
access to its fields.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

class Notification:

    def __init__(self, serialized):
        self.serialized = serialized
        parts = serialized.split('/')
        if parts[0] == 'v2':
            self._parse_v2_notification(parts)
        else:
            self._parse_v1_notification(parts)

    def _parse_v2_notification(self, parts):
        # parts[0] is just "v2"
        self.device_id = parts[1]
        self.notification_id = parts[2]
        self.event_type = parts[3]
        self.data = parts[4]
        self.contents = '/'.join(parts[5:])

    def _parse_v1_notification(self, parts):
        self.device_id = parts[0]
        self.notification_id = parts[1]
        self.event_type = parts[2]
        self.contents = '/'.join(parts[3:])
        self.data = ''

    @property
    def data(self):
        return self.data

    @property
    def device_id(self):
        return self.device_id

    @property
    def notification_id(self):
        return self.notification_id

    @property
    def type(self):
        return self.event_type

    @property
    def contents(self):
        return self.contents

    @property
    def serialized(self):
        return self.serialized
