package chat.simplex.common.security

actual fun ciszaGuardAuthenticate(verify: () -> Boolean): CiszaGuardResult =
  if (try { verify() } catch (_: Exception) { false }) CiszaGuardResult.VERIFIED else CiszaGuardResult.INVALID

actual fun ciszaGuardDisarmOnLockChange() = Unit
