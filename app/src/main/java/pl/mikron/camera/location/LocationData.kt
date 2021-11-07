package pl.mikron.camera.location

import android.location.Location

data class LocationData(

  var previousLocation: Location = Location(""),

  var location: Location = Location("")
)
