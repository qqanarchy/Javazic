#!/bin/bash
# Compilation du projet Javazic
echo "Compilation de Javazic..."
mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm sources.txt
if [ $? -eq 0 ]; then
    echo "Compilation reussie !"
    echo "Lancer avec : java -cp out com.javazic.Main"
else
    echo "Erreur de compilation."
    exit 1
fi
