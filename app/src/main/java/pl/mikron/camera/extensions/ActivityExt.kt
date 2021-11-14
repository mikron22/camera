package pl.mikron.camera.extensions

import android.app.Activity
import android.content.Intent
import pl.mikron.camera.extensions.ContextExt.CREATE_FILE_CODE
import pl.mikron.camera.extensions.ContextExt.OPEN_FILE_CODE

fun Activity.pickFile() {
  val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
    type = "audio/*"
    addCategory(Intent.CATEGORY_OPENABLE)
  }
  startActivityForResult(intent, OPEN_FILE_CODE)
}

fun Activity.createFile() {
  val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "audio/*"
    putExtra(Intent.EXTRA_TITLE, "recording.wav")
  }
  startActivityForResult(intent, CREATE_FILE_CODE)
}

object ContextExt {

  const val OPEN_FILE_CODE = 1

  const val CREATE_FILE_CODE = 2
}