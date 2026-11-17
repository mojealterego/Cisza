package chat.simplex.common.security

import chat.simplex.common.platform.mainActivity

actual fun ciszaGuardAuthenticate(verify: () -> Boolean): CiszaGuardResult {
  val activity = mainActivity.get() ?: return CiszaGuardResult.LOCKED
  return when (CiszaGuard.authenticate(activity.applicationContext, verify)) {
    GuardResult.VERIFIED -> CiszaGuardResult.VERIFIED
    GuardResult.INVALID -> CiszaGuardResult.INVALID
    GuardResult.LOCKED -> CiszaGuardResult.LOCKED
    GuardResult.ERASE_REQUESTED -> CiszaGuardResult.ERASE_REQUESTED
  }
}

actual fun ciszaGuardDisarmOnLockChange() {
  mainActivity.get()?.applicationContext?.let { CiszaGuard.disable(it) }
}
