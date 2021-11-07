package pl.mikron.camera

import android.content.res.Resources
import android.location.Location
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import pl.mikron.camera.location.LocationData
import pl.mikron.camera.location.LocationSensor
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val sensor: LocationSensor,
  private val resources: Resources
) : ViewModel() {

  private val disposables = CompositeDisposable()

  override fun onCleared() {
    super.onCleared()
    disposables.dispose()
  }

  internal fun observeLocation() {
    disposables
      .add(
        sensor
          .observe()
          .subscribe(
            userLocation::postValue
          )
      )
  }

  private val userLocation: MutableLiveData<LocationData> =
    MutableLiveData(LocationData())

  val locationCurrent: LiveData<String> =
    userLocation.map { "${it.location.latitude} ${it.location.longitude}" }

  val locationPrevious: LiveData<String> =
    userLocation.map { "${it.previousLocation.latitude} ${it.previousLocation.longitude}" }

  val velocity: LiveData<String> =
    userLocation.map { resources.getString(R.string.velocity, it.location.speed) }

  val direction: LiveData<String> =
    userLocation.map { resources.getString(R.string.direction, it.location.bearing) }

  val longitudeText: MutableLiveData<String> =
    MutableLiveData(null)

  val latitudeText: MutableLiveData<String> =
    MutableLiveData(null)

  private val _locations: MutableLiveData<List<Location>> =
    MutableLiveData(emptyList())

  val locations: LiveData<List<Pair<Location, Location>>> =
    _locations.switchMap { locations ->
      userLocation.map { current ->
        locations.map {
          Pair(it, current.location)
        }
      }
    }

  fun addLocation() {
    val long = longitudeText.value?.toDouble() ?: return
    val lat = latitudeText.value?.toDouble() ?: return

    _locations.postValue(_locations.value.orEmpty().toMutableList().apply {
      add(
        Location("").apply {
          longitude = long
          latitude = lat
        }
      )
    })
    longitudeText.postValue(null)
    latitudeText.postValue(null)
  }
}
