package pl.mikron.camera

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import pl.mikron.camera.databinding.ActivityMainBinding
import pl.mikron.camera.item.LocationItem

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
    observeLocationWithPermissionCheck()

    val mainAdapter = GenericFastAdapter()
    mainAdapter.addAdapter(0, itemAdapter)

    binding.locationListView.apply {
      adapter = mainAdapter
    }

    observeItems()
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }

  private val itemAdapter: GenericItemAdapter by
    lazy { GenericItemAdapter() }

  private fun observeItems() {
    viewModel
      .locations
      .observe(this) {
        itemAdapter.setNewList(
            it.map(::LocationItem)
        )
      }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    onRequestPermissionsResult(requestCode, grantResults)
  }

  @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
  fun observeLocation() {
    viewModel.observeLocation()
  }
}
