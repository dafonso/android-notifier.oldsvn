"""
Action which copies the contents of the notification to the clipboard.
"""

__author__ = 'wcauchois@gmail.com (William Cauchois)'

import gtk

class CopyAction:
    
    def __init__(self):
        # Get a handle to the default clipboard
        self._clipboard = gtk.Clipboard()
    
    name = 'copy'

    def handle_notification(self, notification):
        self._clipboard.set_text(notification.contents)
