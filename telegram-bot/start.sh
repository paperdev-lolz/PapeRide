#!/usr/bin/env bash
# PapeRide Telegram Bot — start script for VPS / bare metal
set -euo pipefail

cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "ERROR: .env not found. Copy .env.example to .env and fill in your values."
  exit 1
fi

echo "Building PapeRide Telegram Bot..."
mvn -q -DskipTests package

echo "Starting PapeRideBot..."
java -jar target/*.jar