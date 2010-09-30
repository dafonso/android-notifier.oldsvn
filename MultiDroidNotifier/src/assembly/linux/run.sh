#!/bin/sh

configDir=$XDG_CONFIG_HOME
if [ -z $XDG_CONFIG_HOME ]; then
  configDir=$HOME/.config
else
  configDir=$XDG_CONFIG_HOME
fi

# Migrate existing configuration to conform with XDG standard
mkdir -p $configDir/android-notifier-desktop/.java/.userPrefs/com/google/code/notifier/desktop
if [ -d  ~/.java/.userPrefs/com/google/code/notifier ]; then
  mv -f ~/.java/.userPrefs/com/google/code/notifier/desktop/prefs.xml $configDir/android-notifier-desktop/.java/.userPrefs/com/google/code/notifier/desktop
  rm -fr ~/.java/.userPrefs/com/google/code/notifier
fi

java -DconfigDir=$configDir -Djava.util.prefs.userRoot=$configDir/android-notifier-desktop -Djava.net.preferIPv4Stack=true -client -Xms8m -Xmx32m -jar /usr/share/android-notifier-desktop/android-notifier-desktop.jar $1