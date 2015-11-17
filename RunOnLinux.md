>#summary How to use neoeedit on linux/BSD

# Introduction #

use neoeedit itself to make a script for neoeedit


# Details #

Command line usage:
  * change to root(because need to write file to /usr/bin) use "su"
  * $ java -jar neoeedit.jar
  * edit the page like:
```
#!/bin/sh
java -Xmx1024M -jar /home/xxx/Downloads/neoeedit.jar $1 &
```
  * press ctrl-s to save it to /usr/bin/neoeedit.sh
  * close neoeedit
  * $ chmod a+x /usr/bin/neoeedit.sh
  * now you could use "neoeedit.sh your-file" on command line or via GUI.

also you can write a /usr/local/share/applications/neoeedit.desktop file like that:
```
[Desktop Entry]
Name=Neoeedit
Exec=neoeedit.sh %f
Terminal=false
Type=Application
Encoding=UTF-8
Icon=accessories-text-editor
StartupNotify=true
Categories=Utility;TextEditor;
MimeType=text/plain;*/*;
```