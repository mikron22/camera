package pl.mikron.camera.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationSensor @Inject constructor(
  @ApplicationContext private val context: Context
) {

  var locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

  @SuppressLint("MissingPermission")
  fun observe(): Flowable<Location> = Flowable.create({ emitter ->
    locationManager.requestLocationUpdates(GPS_PROVIDER, 5000, 10F, object : LocationListener {

      override fun onLocationChanged(location: Location) {
        emitter.onNext(location)
      }

      override fun onProviderDisabled(provider: String) {}

      override fun onProviderEnabled(provider: String) {}

      override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    })
  }, BackpressureStrategy.BUFFER)
}
