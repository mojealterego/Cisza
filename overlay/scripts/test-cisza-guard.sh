#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TMPDIR:-/tmp}/cisza-guard-tests-${UID}"
mkdir -p "$BUILD"
SRC="$ROOT/apps/multiplatform/common/src/androidMain/kotlin/chat/simplex/common/security/AttemptPolicy.kt"
kotlinc "$SRC" "$ROOT/tests/cisza/AttemptPolicyTest.kt" -include-runtime -d "$BUILD/guard-tests.jar"
java -jar "$BUILD/guard-tests.jar"
