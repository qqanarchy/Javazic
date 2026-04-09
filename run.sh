#!/bin/bash
# Lancement du projet Javazic
cd "$(dirname "$0")"
if [ ! -d "out" ]; then
    bash build.sh
fi
java -cp out com.javazic.Main
