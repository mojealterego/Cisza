package chat.simplex.common.security

/** The desktop build preserves existing authentication without Android-only data wiping. */
enum class CiszaGuardResult { VERIFIED, INVALID, LOCKED, ERASE_REQUESTED }

/** Verifies credentials only after durable reservation when Android guard is armed. */
expect fun ciszaGuardAuthenticate(verify: () -> Boolean): CiszaGuardResult

/** Called only after lock-mode transition has already been authorized. */
expect fun ciszaGuardDisarmOnLockChange()
