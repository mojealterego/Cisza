# CISZA Ω — Secure Messenger v0.1.0

**Android-first fork of SimpleX Chat 7.0.2.** Package ID: `pl.omega.cisza`.

This is a **modified source distribution, not an installable APK**. It includes the original
SimpleX source tree and licensing, Polish resource coverage, a distinct launcher icon,
Android-only opt-in three-attempt protection, and deterministic tests.

## What is actually implemented

- New Android application ID and name, launcher artwork in the Android resource densities.
  Kotlin/JNI package namespaces intentionally remain `chat.simplex.*` to avoid ABI breaks.
- Polish as the initial UI language for new installs, while retaining the user's ability to
  change language. All 2,917 distinct base resource keys have a Polish counterpart.
- Android opt-in **three consecutive incorrect app-PIN attempts** protection. The third
  entered PIN is **verified first**; a correct third PIN resets the counter and never wipes.
- Android Keystore AES-256-GCM authenticated counter, durable file write and atomic rename,
  `fsync` on file and parent directory, conservative lockout on corruption.
- PIN-confirmed activation/deactivation and explicit irreversible-data-loss warning.
- Android `clearApplicationUserData()` request on the third incorrect PIN, after best-effort
  removal of SimpleX-managed Keystore aliases. This removes only this application's own
  private data as controlled by Android. It does not silently uninstall the APK.
- Existing SimpleX transport, Double Ratchet, PQ features and self-destruct passcode retained
  from upstream **without new cryptographic claims**.
- CMake hardening flags for the existing JNI shared library.

## Safety and limitations

**The three-attempt guard is OFF by default.** Only the app-PIN lock mode is covered;
Android biometric/system credential failures are not counted. Three mistaken PINs may
cause irreversible loss of the app's private data. Export a backup first.

No generic Android API gives this app a tamper-proof RPMB monotonic counter. AES-GCM
integrity alone does **not** prevent rollback of a previously valid encrypted state by a
privileged attacker. Files outside app-private storage, remote message copies and external
backups are not erased. Android can reject or interrupt a wipe request. Hardware-backed
Keystore storage depends on device support. The protection is not a guarantee against
laboratory forensic recovery.

The supplied 35-page proposal describes experimental primitives including Split-KEM,
RingXKEM, anamorphic encryption, Path ORAM, sensor-triggered wiping, Poisson traffic
shaping and DTN mesh. Those mechanisms are **NOT implemented or certified** here:
the proposal does not supply audited, interoperable, production-ready protocol definitions
or cryptographic security proofs. Adding them without formal validation would weaken trust.

## Build and test

```bash
./scripts/test-cisza-guard.sh
./scripts/typecheck-cisza-android.sh
./scripts/verify-cisza-source.py
./scripts/build-cisza-android.sh
```

The last command requires Android SDK/NDK, Gradle 8.12 and the original SimpleX Haskell
native libraries (`libsimplex.so` and `libsupport.so`) for the selected ABI. These are
**not present in the supplied source ZIP or the current build environment**. Therefore no
APK or device-level end-to-end test is included. See `docs/CISZA_BUILD.md`.

## Licensing and provenance

Derived from the user-supplied `simplex-chat-stable.zip`, source version 7.0.2. The
upstream license (`LICENSE`, GNU AGPLv3) and third-party notices are preserved.
This is an independent modification, not an official SimpleX Chat release.
