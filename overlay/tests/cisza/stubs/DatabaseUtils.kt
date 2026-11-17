package chat.simplex.common.views.helpers
object DatabaseUtils {
  class KeyStoreItem { fun remove() {} }
  val ksDatabasePassword = KeyStoreItem()
  val ksAppPassword = KeyStoreItem()
  val ksSelfDestructPassword = KeyStoreItem()
}
