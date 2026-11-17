package chat.simplex.common.security

/** State transitions are platform independent; persistence is provided by the Android adapter. */
internal data class GuardState(
  val enabled: Boolean,
  val remaining: Int,
  val terminal: Boolean
) {
  init {
    require(remaining in 0..3) { "Invalid attempt count" }
    require(!terminal || remaining == 0) { "Terminal state must have zero attempts" }
    require(enabled || (!terminal && remaining == 3)) { "Disabled state must be reset" }
  }
}

internal interface GuardStateStore {
  /** Must reject missing/corrupt states once initialized. */
  fun load(): GuardState
  /** Must durably commit before returning, otherwise throw. */
  fun save(value: GuardState)
}

internal enum class GuardResult { VERIFIED, INVALID, LOCKED, ERASE_REQUESTED }

/**
 * Eager-persisted PIN attempt policy.
 * - The third attempt is VERIFIED before deciding whether to erase.
 * - A crash during verification consumes an attempt.
 * - A failed durable write blocks verification.
 * - Wipe failures leave a terminal state; no success is reported.
 * All callers in a process must share one instance; no cross-process guarantee is implied.
 */
internal class AttemptPolicy(
  private val store: GuardStateStore,
  private val requestErase: () -> Boolean
) {
  @Synchronized
  fun authenticate(verify: () -> Boolean): GuardResult {
    val current = store.load()
    if (current.terminal || (current.enabled && current.remaining == 0)) return GuardResult.LOCKED
    if (!current.enabled) return if (safeVerify(verify)) GuardResult.VERIFIED else GuardResult.INVALID

    val reserved = current.copy(remaining = current.remaining - 1)
    store.save(reserved) // durable reservation MUST precede credential comparison

    if (safeVerify(verify)) {
      store.save(GuardState(enabled = true, remaining = 3, terminal = false))
      return GuardResult.VERIFIED
    }
    if (reserved.remaining != 0) return GuardResult.INVALID

    store.save(reserved.copy(terminal = true))
    return if (runCatching { requestErase() }.getOrDefault(false)) {
      GuardResult.ERASE_REQUESTED
    } else {
      GuardResult.LOCKED
    }
  }

  @Synchronized
  fun arm() {
    check(!store.load().terminal) { "Guard is terminal; data recovery is not possible here" }
    store.save(GuardState(enabled = true, remaining = 3, terminal = false))
  }

  @Synchronized
  fun disarm() {
    check(!store.load().terminal) { "Guard is terminal" }
    store.save(GuardState(enabled = false, remaining = 3, terminal = false))
  }

  @Synchronized
  fun state(): GuardState = store.load()

  private fun safeVerify(verify: () -> Boolean): Boolean =
    try { verify() } catch (_: Exception) { false }
}
