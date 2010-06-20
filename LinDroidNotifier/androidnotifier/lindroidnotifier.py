#!/usr/bin/python

"""Entry point for the linux android notifier.

This module starts up the application.
"""

__author__ = 'rodrigo@damazio.org (Rodrigo Damazio Bovendorp)'

import gtk, gtk.glade
import os, sys
import preferences
from manager import NotificationManager

class App:
    @staticmethod
    def get_config_dir():  
        path = os.path.expanduser('~/.android-notifier')
        if not os.path.exists(path):
            os.mkdir(path)
        return path

    def __init__(self, datapath):
        self.datapath = datapath
        self.gladefile = gtk.glade.XML(os.path.join(datapath, 'lindroidnotifier.glade'))

        self.prefs = preferences.Preferences(os.path.join(self.get_config_dir(), 'config'))
        self.prefs.load()
        self.manager = NotificationManager(self.prefs)

        self.prefs_dialog = preferences.PreferencesDialog(self.gladefile, self.prefs, self.manager)
        self.about_dialog = self.gladefile.get_widget('aboutDialog')
        self.gladefile.signal_autoconnect(
          {'on_aboutDialog_response': self._on_aboutDialog_response})

        self.status_icon = gtk.status_icon_new_from_file(os.path.join(datapath, 'menuicon.png'))
        self.status_icon.connect('activate', self._on_status_icon_activate)
        self.status_icon.connect('popup-menu', self._on_status_icon_popup_menu)

    def _on_aboutDialog_response(self, sender, response_id):
        if response_id == gtk.RESPONSE_CANCEL:
            sender.hide()

    def run(self):
        gtk.gdk.threads_init()
        self.manager.start()
        gtk.main()
        self.manager.stop()

    def _on_status_icon_activate(self, status_icon):
        self.prefs_dialog.show()

    def _on_status_icon_popup_menu(self, status_icon, button, activate_time):
        menu = self._build_popup_menu()
        menu.popup(None, None,
          gtk.status_icon_position_menu, button, activate_time, status_icon)

    def _build_popup_menu(self):
        menu = gtk.Menu()

        items = [('Preferences...', lambda sender: self.prefs_dialog.show()),
                 ('About'         , lambda sender: self.about_dialog.show()),
                 ('Quit'          , gtk.main_quit)]
        for (label, handler) in items:
            menu_item = gtk.MenuItem(label)
            menu_item.connect('activate', handler)
            menu.append(menu_item)
            menu_item.show()

        return menu

def check_pid(pid):
    'Check for the existence of a Unix pid.'
    # Sending signal 0 to a pid will raise an OSError exception if the
    # pid is not running, and do nothing otherwise.
    # <http://stackoverflow.com/questions/568271/check-if-pid-is-not-in-use-in-python>
    try:
        os.kill(pid, 0)
    except OSError:
        return False
    else:
        return True

if __name__ == '__main__':
    pidfile = os.path.join(App.get_config_dir(), 'pid')
    if os.path.exists(pidfile):
        pid = int(open(pidfile, 'r').read())
        if check_pid(pid):
            # TODO: open the preferences panel of the currently running android notifier
            print 'Android notifier is already running'
            sys.exit(1)
    open(pidfile, 'w').write('%i' % os.getpid())
    # TODO: detect the data path (for when this is installed somewhere)
    App('../data/').run()
    os.unlink(pidfile)
