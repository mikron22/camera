package pl.mikron.camera.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationSensor @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private var locationManager = LocationServices.getFusedLocationProviderClient(context)
  private val request = LocationRequest.create().apply {
    interval = 10
    maxWaitTime = 20
    fastestInterval = 5
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
  }

  @SuppressLint("MissingPermission")
  fun observe(): Flowable<LocationData> = Flowable.create({ emitter ->
    locationManager.requestLocationUpdates(
      request, object : LocationCallback() {

        private var previousLocation: Location? = null

        override fun onLocationResult(location: LocationResult) {
          if (previousLocation != null) {
            emitter.onNext(
              LocationData(
                previousLocation = previousLocation ?: location.lastLocation,
                location = location.lastLocation
              )
            )
          }
          previousLocation = location.lastLocation
        }
      }, Looper.getMainLooper()
    )
  }, BackpressureStrategy.BUFFER)
}
