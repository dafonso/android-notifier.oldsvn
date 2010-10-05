#!/bin/sh

configDir=$XDG_CONFIG_HOME
if [ -z $XDG_CONFIG_HOME ]; then
  configDir=$HOME/.config
else
  configDir=$XDG_CONFIG_HOME
fi

java -DconfigDir=$configDir -Djava.util.prefs.userRoot=$configDir/android-notifier-desktop -Djava.net.preferIPv4Stack=true -client -Xms8m -Xmx32m -jar ${assembly.jar.path} $1