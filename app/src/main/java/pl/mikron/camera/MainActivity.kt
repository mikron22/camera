package pl.mikron.camera

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import pl.mikron.camera.databinding.ActivityMainBinding
import pl.mikron.camera.utils.CameraProvider
import javax.inject.Inject

@RuntimePermissions
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val disposables = CompositeDisposable()

  private val viewModel: MainViewModel
      by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.viewModel = viewModel
    binding.lifecycleOwner = this
    showCameraWithPermissionCheck()
    observeLocationWithPermissionCheck()
    viewModel.observeSensors()
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    onRequestPermissionsResult(requestCode, grantResults)
  }

  @Inject
  lateinit var cameraProvider: CameraProvider

  @NeedsPermission(Manifest.permission.CAMERA)
  fun showCamera() {
    disposables
      .add(
        cameraProvider
          .initPreview(findViewById(R.id.cameraView), this)
          .subscribe(
            {},
            { Log.e("ERROR", "Error occurred:", it) }
          )
      )
  }

  @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
  fun observeLocation() {
    viewModel.observeLocation()
  }
}