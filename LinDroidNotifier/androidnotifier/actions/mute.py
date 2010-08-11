"""Action which mutes all audio through ALSA.
"""

__author__ = 'wcauchois@gmail.com (Bill Cauchois)'

import os

class MuteAction:
    name = 'mute'

    def handle_notification(self, notification):
        os.system('amixer set Master mute')
