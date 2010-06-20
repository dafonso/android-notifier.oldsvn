"""Action which mutes all audio through ALSA.
"""

__author__ = 'wcauchois@gmail.com (Bill Cauchois)'

import alsaaudio

class MuteAction:
    name = 'mute'

    def handle_notification(self, notification):
        mixer = alsaaudio.Mixer('Master', cardindex=0)
        mixer.setmute(1, alsaaudio.MIXER_CHANNEL_ALL)
