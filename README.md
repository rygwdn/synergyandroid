# SynergyAndroid

This directory contains the SynergyAndroid project source code. This was originally
maintained at http://sourceforge.net/projects/synergyandroid/.


## Introduction

SynergyAndroid is a port of the synergy client to the Android platform. 
See http://synergy-foss.org for detail. Currently in the alpha stage.
New developers are very welcome to join in.


## Pre-requirements

We use the uinput module for user space input driver, so you must have
a modded Android with access to /dev/uinput (Like Cyanogen). That means
your android must have /dev/uinput module and this file can be RW by app.

If you want to test with an emulator, I recommend using
[Genymotion](http://www.genymotion.com/).
 

## Building the project

SynergyAndroid use the jni to access the /dev/uinput module, so you must
have the android NDK available. If you try to build the app without the
NDK configured, it will produce an error with instructions.

This project _should_ work in Android Studio, and IntelliJ. You can
also build at the command line with `./gradlew assembleDebug`.
