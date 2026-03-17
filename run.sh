#!/usr/bin/env zsh

# run.sh - compile and run HotelManagementApp
# Usage:
#   ./run.sh           # compile and run interactively
#   ./run.sh test      # compile and run with a built-in non-interactive test
#   ./run.sh compile   # only compile

# Adjust this path if your connector is elsewhere
JAR_PATH="./mysql-connector-j-9.4.0/mysql-connector-j-9.4.0.jar"
SRC=HotelManagementApp.java
MAIN_CLASS=HotelManagementApp

set -euo pipefail

if [[ ! -f "$JAR_PATH" ]]; then
  echo "MySQL connector JAR not found at $JAR_PATH"
  echo "Please update JAR_PATH in run.sh or place the connector at that location."
  exit 2
fi

echo "Compiling $SRC..."
javac -cp ".:$JAR_PATH" "$SRC"

action=${1:-run}

if [[ "$action" == "compile" ]]; then
  echo "Compiled. To run: java -cp .:$JAR_PATH $MAIN_CLASS"
  exit 0
fi

if [[ "$action" == "test" ]]; then
  echo "Running non-interactive test (attempt to reserve guestId=9999, roomId=9999)..."
  printf "4\n9999\n9999\n2025-10-20\n2025-10-22\n6\n" | java -cp ".:$JAR_PATH" $MAIN_CLASS
  exit $?
fi

# Default: run interactively
echo "Starting $MAIN_CLASS (interactive). Press Ctrl+C to stop." 
java -cp ".:$JAR_PATH" $MAIN_CLASS
