#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TMPDIR:-/tmp}/cisza-android-typecheck-${UID}"
mkdir -p "$BUILD"
SEC="$ROOT/apps/multiplatform/common/src/androidMain/kotlin/chat/simplex/common/security"
kotlinc "$SEC/AttemptPolicy.kt" "$SEC/CiszaGuard.kt" "$ROOT/tests/cisza/stubs/"*.kt -d "$BUILD/cisza-guard-android-stubcheck.jar"
echo 'PASS Android guard Kotlin syntax/types against test API stubs (not a real Android build)'
