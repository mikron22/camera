package pl.mikron.camera.utils

import android.content.Context
import android.view.Surface.ROTATION_0
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@ActivityScoped
class CameraProvider @Inject constructor(
  @ApplicationContext private val context: Context
  ) {

  fun initPreview(view: PreviewView, lifecycle: LifecycleOwner): Completable =
    getProvider()
      .flatMapCompletable {

        val preview =
          Preview
            .Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(ROTATION_0)
            .build()

        val selector =
          CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(view.surfaceProvider)

        it.bindToLifecycle(lifecycle, selector, preview)

        Completable.complete()
      }





  private fun getProvider(): Single<ProcessCameraProvider> =
    Single.create { emitter ->
      val instanceListener = ProcessCameraProvider.getInstance(context)
      instanceListener.addListener({
        emitter.onSuccess(instanceListener.get())
      }, ContextCompat.getMainExecutor(context))
    }
}
