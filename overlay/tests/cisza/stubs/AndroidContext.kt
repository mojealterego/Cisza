package android.content
import java.io.File
open class Context {
  open val noBackupFilesDir: File get() = File("/tmp")
  open val packageName: String get() = "pl.omega.cisza"
  open val applicationContext: Context get() = this
  open fun getSystemService(name: String): Any? = null
  companion object { const val ACTIVITY_SERVICE: String = "activity" }
}
