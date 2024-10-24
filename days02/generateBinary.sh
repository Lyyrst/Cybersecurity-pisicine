#!/bin/bash

javac src/*.java
jar cfm ft_otp.jar MANIFEST.MF -C src .
chmod +x ft_otp.jar