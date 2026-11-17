#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MP="$ROOT/apps/multiplatform"
ABI="${CISZA_ANDROID_ABI:-arm64-v8a}"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" || ! -d "$SDK" ]]; then
  echo 'ERROR: Android SDK not configured. Set ANDROID_HOME or ANDROID_SDK_ROOT.' >&2
  exit 2
fi
if ! command -v java >/dev/null 2>&1; then
  echo 'ERROR: Java 17+ is required.' >&2
  exit 2
fi
for name in libsimplex.so libsupport.so; do
  f="$MP/common/src/commonMain/cpp/android/libs/$ABI/$name"
  if [[ ! -s "$f" ]]; then
    echo "ERROR: Missing native dependency: $f" >&2
    echo 'Build the original SimpleX Haskell core and support libraries for the selected Android ABI first.' >&2
    exit 3
  fi
done
cd "$MP"
./gradlew :android:assembleFossDebug --no-daemon
printf '\nAPK outputs:\n'
find android/build/outputs/apk -type f -name '*.apk' -print
