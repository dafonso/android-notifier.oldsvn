"""Action which displays the notification on the screen.

This action will display the notification using pynotify.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

import pynotify
import notification

class DisplayAction:

    def __init__(self):
        if not pynotify.init('LinDroidNotifier'):
            raise RuntimeError, 'Unable to initialize notifications'

    @property
    def name(self):
        return 'display'

    def handle_notification(self, notification):
        title = self._notification_title(notification)
        description = notification.contents
        # TODO: icon (see /usr/share/doc/python-notify/examples/test-image.py)
        n = pynotify.Notification(title, description)
        n.show()

    def _notification_title(self, notification):
        type = notification.type
        # TODO: i18n
        if (type == 'RING'):
            return 'Phone is ringing'
        elif (type == 'BATTERY'):
            return 'Phone battery state'
        elif (type == 'SMS'):
            return 'Phone received an SMS'
        elif (type == 'MMS'):
            return 'Phone received an MMS'
        elif (type == 'PING'):
            return 'Phone sent a ping'
        elif (type == 'USER'):
            return notification.data
        else:
            return "Unknown notification: %s" % type
