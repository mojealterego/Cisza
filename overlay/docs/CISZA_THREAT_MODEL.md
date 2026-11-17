# CISZA Ω — security threat model

## Assets and trust boundaries

Assets: app-private messages, contact database, XFTP metadata, locally cached attachments,
Keystore-wrapped secrets, app-PIN authentication state.
Trust boundaries: Compose UI → local PIN verifier → Android security policy → Android
Keystore → encrypted no-backup state file → Android ActivityManager data-clearing API.

## Defended scenarios

- Casual PIN guessing against the app's own PIN screen, **when the user explicitly arms
  the guard**. Every attempt is reserved on disk before verification.
- Process restart after an incorrect PIN: the reduced attempt count remains on disk.
- Tampering that changes the AES-GCM record without the key: authentication is blocked.
- Concurrent attempts in one process: serialized by synchronized policy transitions.

## Residual threats and trade-offs

- A privileged attacker can restore a previously valid encrypted counter snapshot. AES-GCM
  does not provide a monotonic hardware counter; no RPMB access is claimed.
- A crash after reserving the third attempt but before verifying it may lock the app without
  wiping. This avoids automatically deleting data after an unverified third PIN.
- A damaged Keystore or missing state causes fail-closed lockout and may require clearing
  application data to recover. The software does not claim secure key recovery.
- Clearing app-private data does not remove data already exported, cloud backups, recipient
  copies, shared storage or data outside Android's managed sandbox.
- Android's data-clear API may fail, be interrupted, or finish asynchronously. The app
  records the terminal state before requesting removal and does not report a completed wipe
  unless Android confirms it (which is not asserted in this version).
- Android Keystore may be software-backed depending on device capabilities.
- The original upstream code still uses immutable JVM `String` for PIN comparison. This
  derivative does not claim to have eliminated all secret copies from managed memory.
- Existing cryptographic and network behavior is inherited; it has not been formally
  re-audited in this build.

## Out of scope

Silent package uninstall, destructive actions outside the app sandbox, speculative
post-quantum primitives, custom sensor-triggered wiping, root/bootloader compromise,
external storage erasure, and forensic-proof physical NAND destruction.
