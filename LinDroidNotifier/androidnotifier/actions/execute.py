"""Action which executes the user-specified command.
"""

import subprocess, os

__author__ = 'wcauchois@gmail.com (Bill Cauchois)'

class ExecuteAction:
    name = 'execute'

    def __init__(self, get_execute_target):
        self.get_execute_target = get_execute_target

    def handle_notification(self, notification):
        execute_target = self.get_execute_target()
        if execute_target is None:
            return
        env = dict(os.environ)
        env['NOTIFICATION_DEVICE_ID'] = notification.device_id
        env['NOTIFICATION_TYPE'] = notification.type
        env['NOTIFICATION_CONTENTS'] = notification.contents
        env['NOTIFICATION_DATA'] = notification.data
        env['NOTIFICATION_SERIALIZED'] = notification.serialized
        subprocess.call(execute_target, executable=execute_target, env=env)
