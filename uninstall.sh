#!/bin/sh


package_dir=$(cd `dirname $0` && pwd)

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

rm /usr/share/applications/nixnote.desktop
rm -rf /usr/share/nixnote

echo "Uninstall completed"
