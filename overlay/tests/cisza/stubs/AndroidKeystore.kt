package android.security.keystore
import java.security.spec.AlgorithmParameterSpec
object KeyProperties {
 const val KEY_ALGORITHM_AES="AES"
 const val PURPOSE_ENCRYPT=1
 const val PURPOSE_DECRYPT=2
 const val BLOCK_MODE_GCM="GCM"
 const val ENCRYPTION_PADDING_NONE="NoPadding"
}
class KeyGenParameterSpec : AlgorithmParameterSpec {
 class Builder(alias: String, purposes: Int) {
  fun setBlockModes(vararg modes: String): Builder = this
  fun setEncryptionPaddings(vararg modes: String): Builder = this
  fun setKeySize(size: Int): Builder = this
  fun setRandomizedEncryptionRequired(value: Boolean): Builder = this
  fun build(): KeyGenParameterSpec = KeyGenParameterSpec()
 }
}
