// UriUtil.kt
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

fun Uri.getRealPath(context: Context): String? {
    if (this.scheme == "file") {
        return this.path
    }
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    var cursor = context.contentResolver.query(this, projection, null, null, null)

    return cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        if (it.moveToFirst()) {
            it.getString(columnIndex)
        } else {
            null
        }
    }
}

fun Uri.toFile(context: Context): File? {
    val path = getRealPath(context)
    return if (path != null) File(path) else null
}