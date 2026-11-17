package android.system
import java.io.FileDescriptor
object Os { fun open(path: String, flags: Int, mode: Int): FileDescriptor = FileDescriptor(); fun fsync(fd: FileDescriptor) {}; fun close(fd: FileDescriptor) {} }
object OsConstants { const val O_RDONLY = 0 }
