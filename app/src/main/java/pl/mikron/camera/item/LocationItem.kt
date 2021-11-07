package pl.mikron.camera.item

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.ModelAbstractBindingItem
import pl.mikron.camera.R
import pl.mikron.camera.databinding.ItemLocationBinding

class LocationItem(model: Pair<Location, Location>) :
  ModelAbstractBindingItem<Pair<Location, Location>, ItemLocationBinding>(model) {

  init {
    identifier = model.first.time
  }

  override val type: Int
    get() = 0

  override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemLocationBinding =
    ItemLocationBinding.inflate(inflater, parent, false)

  override fun bindView(binding: ItemLocationBinding, payloads: List<Any>) {
    super.bindView(binding, payloads)
    binding.model = model.first

    val location = model.first
    val currentLocation = model.second

    val direction = currentLocation.bearingTo(location)
    val distance = currentLocation.distanceTo(location)

    binding.directionView.text = binding.root.context.getString(R.string.direction, direction)
    binding.distanceView.text = binding.root.context.getString(R.string.distance, distance)

    binding.imageView.rotation = direction
  }

  override fun unbindView(binding: ItemLocationBinding) {
    super.unbindView(binding)
    binding.latitudeView.text = null
    binding.longitudeView.text = null
    binding.distanceView.text = null
    binding.directionView.text = null
  }
}