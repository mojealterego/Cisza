# CISZA Ω — build instructions and release criteria

## Source and build prerequisites

1. Linux/macOS build host with Java 21 (Gradle 8.12 compatible), Git, CMake and network access
   to Google Maven/Maven Central/Gradle distributions.
2. Android SDK Platform 35, Android Gradle Plugin 8.7.0, Android NDK 23.1.7779620,
   and the CMake toolchain required by the SimpleX upstream project.
3. Rebuild the **unmodified upstream Haskell core** and its support library for each target
   ABI. Provide:

```
apps/multiplatform/common/src/commonMain/cpp/android/libs/arm64-v8a/libsimplex.so
apps/multiplatform/common/src/commonMain/cpp/android/libs/arm64-v8a/libsupport.so
```

4. From the source root, run `./scripts/build-cisza-android.sh`.
   Output is expected under `apps/multiplatform/android/build/outputs/apk/`.
5. Sign a release APK using a private signing key outside this repository. Do not commit
   keystores or passwords. Run the Android instrumentation suite and real-device tests.

## Required verification before distribution

- Build a signed APK/AAB for `arm64-v8a` on an isolated runner.
- Install on Android API 26, 29, 33 and 35 devices/emulators.
- Verify contacts, SMP messaging, XFTP, calling, backups, Tor/Orbot proxy settings.
- Verify PIN activation, third-correct-PIN behavior, third-wrong-PIN deletion on a test
  profile, crash/restart persistence, biometric-mode exclusion, and storage corruption.
- Verify Android Keystore behavior and `clearApplicationUserData()` on target devices.
- Verify language resources and layouts in Polish on small screens and landscape.
- Audit application-ID changes for notifications, intents, FileProvider and migration.
- Run dependency and supply-chain checks; produce signed hashes and SBOM.

**Important:** A different Android application ID creates a separate application. This is
not an in-place update to official SimpleX Chat; data migration requires a tested export/import
workflow. Do not uninstall an existing installation before confirming your backup.

## Exact current verification status

- 16 deterministic JVM unit/concurrency/fault-injection checks: **passed**.
- Android Keystore implementation: Kotlin type-checked against API stubs; **not compiled
  against the real Android SDK**.
- XML/Polish resource and manifest static checks: **passed**.
- Native SimpleX core, Gradle Android build, Android device E2E: **not performed**.
- An attempted `./gradlew :android:assembleFossDebug --offline` could not download the
  Gradle 8.12 distribution because the build environment lacks external DNS/network.

No APK is claimed, no native-cryptography verification is claimed, and no security
certification is claimed.
