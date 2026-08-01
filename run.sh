#!/bin/bash
# Launches the Javions GUI.
#
# Usage:
#   ./run.sh                     -> reads live data from the AirSpy SDR (stdin)
#   ./run.sh <messages.bin>      -> replays a recorded messages file
#
# Example (replay the provided recording):
#   ./run.sh resources/messages_20230318_0915.bin

set -e
cd "$(dirname "$0")"

JFX="$HOME/tools/javafx-sdk"
CLASSES=out/classes

# Recompile if sources are newer than the compiled output
if [ ! -d "$CLASSES" ] || find src -name "*.java" -newer "$CLASSES" -print -quit | grep -q .; then
  echo "Compiling..."
  mkdir -p "$CLASSES"
  cp resources/* "$CLASSES"
  javac --module-path "$JFX/lib" --add-modules javafx.controls,javafx.swing \
        -d "$CLASSES" $(find src -name "*.java")
fi

exec java --module-path "$JFX/lib" --add-modules javafx.controls,javafx.swing \
     -cp "$CLASSES" ch.epfl.javions.gui.Main "$@"
