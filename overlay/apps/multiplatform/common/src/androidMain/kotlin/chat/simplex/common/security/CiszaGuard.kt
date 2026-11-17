package chat.simplex.common.security

import android.app.ActivityManager
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.system.Os
import android.system.OsConstants
import chat.simplex.common.views.helpers.DatabaseUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * An opt-in, fail-closed Android guard. The key is in Android Keystore (hardware-backed only
 * when supported by the device). AES-GCM authenticates the state but DOES NOT prevent a
 * privileged attacker from rolling back an old encrypted file: no generic Android RPMB API.
 */
internal class EncryptedGuardStore(private val context: Context) : GuardStateStore {
  private val alias = "pl.omega.cisza.guard.counter.v1"
  private val file = File(context.noBackupFilesDir, "cisza_guard_state.v1")
  private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  private val aad = "CISZA-GUARD-v1:${context.packageName}".toByteArray(Charsets.UTF_8)

  @Synchronized
  override fun load(): GuardState {
    val hasKey = keyStore.containsAlias(alias)
    val hasState = file.isFile
    if (!hasKey && !file.exists()) {
      generateKey()
      val initial = GuardState(enabled = false, remaining = 3, terminal = false)
      save(initial)
      return initial
    }
    // Never reinitialize an already existing but missing/corrupt state.
    if (!hasKey || !hasState) throw SecurityException("CISZA guard state missing or key unavailable")
    val bytes = file.readBytes()
    if (bytes.size < 5 + 12 + 16 || bytes.size > 256) throw SecurityException("Invalid guard record length")
    if (!bytes.copyOfRange(0, 4).contentEquals(byteArrayOf(67, 90, 71, 49))) {
      throw SecurityException("Invalid guard record version")
    }
    val ivSize = bytes[4].toInt() and 0xff
    if (ivSize != 12) throw SecurityException("Invalid guard IV length")
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(5, 5 + ivSize)))
    cipher.updateAAD(aad)
    val payload = cipher.doFinal(bytes.copyOfRange(5 + ivSize, bytes.size))
    if (payload.size != 4 || payload[0].toInt() != 1 || payload[1].toInt() !in 0..1 ||
      payload[2].toInt() !in 0..3 || payload[3].toInt() !in 0..1) {
      throw SecurityException("Invalid guard state payload")
    }
    return GuardState(payload[1].toInt() == 1, payload[2].toInt(), payload[3].toInt() == 1)
  }

  @Synchronized
  override fun save(value: GuardState) {
    val plain = byteArrayOf(1, (if (value.enabled) 1 else 0).toByte(), value.remaining.toByte(), (if (value.terminal) 1 else 0).toByte())
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, key()) // Keystore generates a fresh, randomized IV
    cipher.updateAAD(aad)
    val iv = cipher.iv
    require(iv.size == 12) { "Unexpected GCM IV length" }
    val encrypted = cipher.doFinal(plain)
    plain.fill(0)
    val record = byteArrayOf(67, 90, 71, 49, iv.size.toByte()) + iv + encrypted
    val tmp = File(file.parentFile, "${file.name}.tmp")
    try {
      FileOutputStream(tmp).use { output ->
        output.write(record)
        output.fd.sync()
      }
      // Same-directory rename is atomic. No unsafe copy fallback.
      Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
      val dirFd = Os.open(file.parentFile!!.absolutePath, OsConstants.O_RDONLY, 0)
      try { Os.fsync(dirFd) } finally { Os.close(dirFd) }
    } finally {
      if (tmp.exists()) tmp.delete()
    }
  }

  private fun key(): SecretKey = keyStore.getKey(alias, null) as? SecretKey
    ?: throw SecurityException("Missing guard key")

  private fun generateKey() {
    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .setKeySize(256)
      .setRandomizedEncryptionRequired(true)
      .build()
    generator.init(spec)
    generator.generateKey()
  }
}

/** API used by Android UI and the cross-platform authentication bridge. */
internal object CiszaGuard {
  enum class Status { DISABLED, ARMED, LOCKED }

  @Volatile private var policyInstance: AttemptPolicy? = null

  @Synchronized
  private fun policy(context: Context): AttemptPolicy {
    policyInstance?.let { return it }
    val appContext = context.applicationContext
    val policy = AttemptPolicy(EncryptedGuardStore(appContext)) { eraseOwnAppData(appContext) }
    policyInstance = policy
    return policy
  }

  fun status(context: Context): Status = try {
    val state = policy(context).state()
    when {
      state.terminal -> Status.LOCKED
      state.enabled -> Status.ARMED
      else -> Status.DISABLED
    }
  } catch (_: Exception) { Status.LOCKED }

  fun authenticate(context: Context, verify: () -> Boolean): GuardResult =
    try { policy(context).authenticate(verify) }
    catch (_: Exception) { GuardResult.LOCKED }

  /** Caller must verify the user's current app passcode before invoking this. */
  fun enable(context: Context): Boolean =
    runCatching { policy(context).arm(); true }.getOrDefault(false)

  /** Caller must verify the user's current app passcode before invoking this. */
  fun disable(context: Context): Boolean =
    runCatching { policy(context).disarm(); true }.getOrDefault(false)

  private fun eraseOwnAppData(context: Context): Boolean {
    // Best-effort removal of SimpleX-managed Keystore aliases, then Android's private-data wipe.
    // Data stored by peers, external backups and SAF files are outside this application's control.
    runCatching { DatabaseUtils.ksDatabasePassword.remove() }
    runCatching { DatabaseUtils.ksAppPassword.remove() }
    runCatching { DatabaseUtils.ksSelfDestructPassword.remove() }
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      ?: return false
    return manager.clearApplicationUserData()
  }
}
