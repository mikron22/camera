package pl.mikron.camera

import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import pl.mikron.camera.location.LocationSensor
import pl.mikron.camera.sensors.Sensors
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class MainViewModel @Inject constructor(
  private val sensors: Sensors,
  private val location: LocationSensor
) : ViewModel() {

  private val locationG = Location("")
  private val locationS = Location("")
  private val locationDefault = Location("")

  init {
    locationG.latitude = 18.619495
    locationG.longitude = 54.371797
    locationS.latitude = 18.572433
    locationS.longitude = 54.446862
    locationDefault.latitude = 18.574557
    locationDefault.longitude = 54.408253
  }

  private val disposables = CompositeDisposable()

  override fun onCleared() {
    super.onCleared()
    disposables.dispose()
  }

  private val cameraVector = floatArrayOf(0f, 0f, -1f)

  internal fun observeSensors() {
    disposables
      .add(
        sensors
          .observe()
          .subscribe(
            _rotation::postValue,
          )
      )
  }

  internal fun observeLocation() {
    disposables
      .add(
        location
          .observe()
          .subscribe(
            _userLocation::postValue
          )
      )
  }

  private val _userLocation: MutableLiveData<Location> =
    MutableLiveData(locationDefault)

  val locationU: LiveData<String> =
    _userLocation.map { "${it.latitude} ${it.longitude}" }

  private val _rotation: MutableLiveData<FloatArray> =
    MutableLiveData(FloatArray(9))

  private val _orientation: LiveData<FloatArray> =
    _rotation.map { SensorManager.getOrientation(it, FloatArray(3)) }

  val orientationText: LiveData<String> =
    _orientation.map { it.joinToString { element -> element.toDegrees().toString() } }

  private val _deviceVector: LiveData<FloatArray> =
    _rotation.map {
      val vector = FloatArray(3)
      vector[0] = it[0] * cameraVector[0] + it[1] * cameraVector[1] + it[2] * cameraVector[2]
      vector[1] = it[3] * cameraVector[0] + it[4] * cameraVector[1] + it[5] * cameraVector[2]
      vector[2] = it[6] * cameraVector[0] + it[7] * cameraVector[1] + it[8] * cameraVector[2]
      return@map vector
    }

  val deviceVectorText: LiveData<String> =
    _deviceVector.map { it.joinToString() }

  val angleG: LiveData<String> =
    _deviceVector.switchMap { device ->
      _userLocation.map { location ->
        val angle = location.bearingTo(locationG)
        val vector = FloatArray(3)
        vector[0] = sin(angle)
        vector[1] = cos(angle)
        vector[2] = 0F
        getAngle(vector, device).toDegrees().toString()
      }
    }

  val angleS: LiveData<String> =
    _deviceVector.switchMap { device ->
      _userLocation.map { location ->
        val angle = location.bearingTo(locationS)
        val vector = FloatArray(3)
        vector[0] = sin(angle)
        vector[1] = cos(angle)
        vector[2] = 0F
        getAngle(vector, device).toDegrees().toString()
      }
    }

  private fun Float.toDegrees(): Int {
    if (this.isNaN()) return 0
    return ((this * 180) / PI).roundToInt()
  }

  private fun dotProduct(a: FloatArray, b: FloatArray) =
    a[0] * b[0] + a[1] * b[1] + a[2] * b[2]

  private fun vectorMagnitude(vec: FloatArray) =
    sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2])

  private fun getAngle(a: FloatArray, b: FloatArray) =
    acos(dotProduct(a, b) / (vectorMagnitude(a) * vectorMagnitude(b)))
}
