package chat.simplex.common.security

private class MemoryStore(initial: GuardState) : GuardStateStore {
  var state = initial
  var writes = 0
  var failNextWrite = false
  override fun load(): GuardState = state
  override fun save(value: GuardState) {
    if (failNextWrite) { failNextWrite = false; error("disk failure") }
    state = value
    writes++
  }
}

private fun checkCase(name: String, body: () -> Unit) {
  try { body(); println("PASS $name") }
  catch (error: Throwable) { System.err.println("FAIL $name: $error"); throw error }
}

fun main() {
  checkCase("disabled does not mutate or erase") {
    val store = MemoryStore(GuardState(false, 3, false))
    var wipes = 0
    val policy = AttemptPolicy(store) { wipes++; true }
    check(policy.authenticate { false } == GuardResult.INVALID)
    check(policy.authenticate { true } == GuardResult.VERIFIED)
    check(store.writes == 0 && wipes == 0)
  }
  checkCase("two incorrect attempts consume durable slots") {
    val store = MemoryStore(GuardState(true, 3, false))
    val policy = AttemptPolicy(store) { error("should not wipe") }
    check(policy.authenticate { false } == GuardResult.INVALID)
    check(store.state.remaining == 2)
    check(policy.authenticate { false } == GuardResult.INVALID)
    check(store.state.remaining == 1)
  }
  checkCase("third attempt with correct credential never wipes") {
    val store = MemoryStore(GuardState(true, 1, false))
    var wipes = 0
    val policy = AttemptPolicy(store) { wipes++; true }
    check(policy.authenticate { true } == GuardResult.VERIFIED)
    check(store.state.remaining == 3 && !store.state.terminal && wipes == 0)
  }
  checkCase("third wrong credential requests wipe exactly once") {
    val store = MemoryStore(GuardState(true, 1, false))
    var wipes = 0
    val policy = AttemptPolicy(store) { wipes++; true }
    check(policy.authenticate { false } == GuardResult.ERASE_REQUESTED)
    check(store.state.remaining == 0 && store.state.terminal)
    check(policy.authenticate { true } == GuardResult.LOCKED)
    check(wipes == 1)
  }
  checkCase("crash during verifier consumes slot") {
    val store = MemoryStore(GuardState(true, 3, false))
    val policy = AttemptPolicy(store) { true }
    check(policy.authenticate { error("process interrupted") } == GuardResult.INVALID)
    check(store.state.remaining == 2)
    val resumed = AttemptPolicy(store) { true }
    check(resumed.authenticate { true } == GuardResult.VERIFIED)
    check(store.state.remaining == 3)
  }
  checkCase("failed persistence prevents verification") {
    val store = MemoryStore(GuardState(true, 3, false))
    store.failNextWrite = true
    var verified = false
    val policy = AttemptPolicy(store) { true }
    check(runCatching { policy.authenticate { verified = true; true } }.isFailure)
    check(!verified)
  }
  checkCase("failed wipe remains locked") {
    val store = MemoryStore(GuardState(true, 1, false))
    val policy = AttemptPolicy(store) { false }
    check(policy.authenticate { false } == GuardResult.LOCKED)
    check(store.state.terminal)
  }
  checkCase("armed state cannot reset on incorrect passcode") {
    val store = MemoryStore(GuardState(true, 3, false))
    val policy = AttemptPolicy(store) { true }
    check(policy.authenticate { false } == GuardResult.INVALID)
    check(store.state.remaining == 2)
    check(policy.authenticate { true } == GuardResult.VERIFIED)
    check(store.state.remaining == 3)
  }
  checkCase("terminal state cannot be armed again") {
    val store = MemoryStore(GuardState(true, 0, true))
    val policy = AttemptPolicy(store) { true }
    check(runCatching { policy.arm() }.isFailure)
  }
  checkCase("successful credential resets after two failures") {
    val store = MemoryStore(GuardState(true, 3, false))
    val policy = AttemptPolicy(store) { error("unexpected erase") }
    check(policy.authenticate { false } == GuardResult.INVALID)
    check(policy.authenticate { false } == GuardResult.INVALID)
    check(policy.authenticate { true } == GuardResult.VERIFIED)
    check(policy.authenticate { false } == GuardResult.INVALID)
    check(store.state.remaining == 2)
  }
  checkCase("verifier is invoked once per permitted attempt") {
    val store = MemoryStore(GuardState(true, 2, false))
    var count = 0
    val policy = AttemptPolicy(store) { true }
    check(policy.authenticate { count++; false } == GuardResult.INVALID)
    check(count == 1)
  }
  checkCase("failed reset cannot return success") {
    val store = object : GuardStateStore {
      var state = GuardState(true, 1, false)
      override fun load(): GuardState = state
      override fun save(value: GuardState) {
        if (value.remaining == 3) error("reset disk failure")
        state = value
      }
    }
    val policy = AttemptPolicy(store) { true }
    check(runCatching { policy.authenticate { true } }.isFailure)
    check(store.state.remaining == 0 && !store.state.terminal)
    check(policy.authenticate { true } == GuardResult.LOCKED)
  }
  checkCase("concurrent incorrect attempts erase at most once") {
    val store = MemoryStore(GuardState(true, 3, false))
    var wipes = 0
    val policy = AttemptPolicy(store) { wipes++; true }
    val start = java.util.concurrent.CountDownLatch(1)
    val threads = (1..24).map {
      Thread { start.await(); policy.authenticate { false } }.apply { start() }
    }
    start.countDown()
    threads.forEach { it.join() }
    check(wipes == 1)
    check(store.state.terminal && store.state.remaining == 0)
  }
  checkCase("corrupted storage never authenticates") {
    val store = object : GuardStateStore {
      override fun load(): GuardState = throw SecurityException("invalid authentication tag")
      override fun save(value: GuardState) = error("should not write")
    }
    val policy = AttemptPolicy(store) { error("should not erase") }
    var verifierCalled = false
    check(runCatching { policy.authenticate { verifierCalled = true; true } }.isFailure)
    check(!verifierCalled)
  }
  checkCase("terminal state cannot be disarmed") {
    val store = MemoryStore(GuardState(true, 0, true))
    val policy = AttemptPolicy(store) { true }
    check(runCatching { policy.disarm() }.isFailure)
  }
  checkCase("arm and disarm are explicit") {
    val store = MemoryStore(GuardState(false, 3, false))
    val policy = AttemptPolicy(store) { true }
    policy.arm()
    check(store.state == GuardState(true, 3, false))
    policy.disarm()
    check(store.state == GuardState(false, 3, false))
  }
}
