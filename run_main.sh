#!/usr/bin/env bash
set -euo pipefail

echo "===> Compiling…"
mvn -q clean compile

echo "===> Running MainApp…"
mvn -q exec:java -Dexec.mainClass="app.MainApp"
