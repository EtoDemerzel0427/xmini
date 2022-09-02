#!/bin/bash


cd src || exit  # if fail then exit
rm -rf XMini/*.class
javac XMini/*.java
if $1
then
    java XMini.XMini
else
    java XMini.XMini ../"$1"
fi
