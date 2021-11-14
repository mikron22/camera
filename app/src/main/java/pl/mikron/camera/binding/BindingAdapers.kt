package pl.mikron.camera.binding

import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter

@BindingAdapter("app:tint")
fun ImageView.setImageTint(@ColorInt color: Int) {
  setColorFilter(color)
}
