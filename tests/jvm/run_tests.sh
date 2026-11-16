#!/usr/bin/env bash
# Kompiliert die plattformunabhängige Logik (chess/, engine/, learn/) zusammen mit den Tests
# und führt sie aus. Voraussetzungen: JDK 17+, kotlinc 1.9.x, chesslib-Jar und commons-lang3-Jar.
#   KOTLINC=/pfad/kotlinc/bin/kotlinc CHESSLIB=/pfad/chesslib-1.3.4.jar LANG3=/pfad/commons-lang3.jar ./run_tests.sh
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
KOTLINC="${KOTLINC:-kotlinc}"
CHESSLIB="${CHESSLIB:?CHESSLIB=pfad/zu/chesslib-1.3.4.jar setzen}"
LANG3="${LANG3:?LANG3=pfad/zu/commons-lang3.jar setzen}"
OUT="${OUT:-$(mktemp -d)}"
SRC="$ROOT/app/src/main/java/com/chessacademy/ai"
KOTLIN_LIB="$(dirname "$KOTLINC")/../lib"
COROUTINES="${COROUTINES:-$KOTLIN_LIB/kotlinx-coroutines-core-jvm.jar}"
FILES=$(find "$SRC/chess" "$SRC/engine" "$SRC/learn" -name '*.kt' 2>/dev/null)
if ! "$KOTLINC" -nowarn -cp "$CHESSLIB:$LANG3:$COROUTINES" -d "$OUT" $FILES "$HERE"/*.kt > "$OUT/compile.log" 2>&1; then
  grep -v "JAVA_TOOL_OPTIONS" "$OUT/compile.log"; echo "KOMPILIERFEHLER"; exit 1
fi
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "$OUT:$CHESSLIB:$LANG3:$KOTLIN_LIB/kotlin-stdlib.jar:$KOTLIN_LIB/kotlinx-coroutines-core-jvm.jar" com.chessacademy.ai.tests.MainKt "$@"
