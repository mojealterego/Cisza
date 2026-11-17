# CISZA Ω 0.1.0 — change log

- Fork identity: CISZA Ω, `pl.omega.cisza`, Android launcher icon and Polish UI default.
- Complete Polish resource key coverage relative to upstream SimpleX 7.0.2: +378 translations.
- Android opt-in three-attempt guard: encrypted durable counter, authenticated toggle,
  corrected third-attempt verification, and Android app-private-data clear request.
- Added tests for successful/failed third attempt, crash, write failures, corruption,
  terminal state and concurrent attempts.
- Hardened JNI CMake compile/link flags and disabled fragile-data retention prompt.
- Preserved original SimpleX source, license and notices.

**Release classification:** SOURCE / ENGINEERING VALIDATION, NOT INSTALLABLE APK.
