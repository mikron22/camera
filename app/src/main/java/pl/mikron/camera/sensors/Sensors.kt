package pl.mikron.camera.sensors

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Sensors @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager

  fun observe(): Flowable<FloatArray> = Flowable.combineLatest(
    observeSensor(Sensor.TYPE_GRAVITY),
    observeSensor(Sensor.TYPE_MAGNETIC_FIELD)
  )
  { gravityData, magneticData ->
    val r = FloatArray(9)
    val i = FloatArray(9)
    SensorManager.getRotationMatrix(r, i, gravityData.values, magneticData.values)

    return@combineLatest r
  }

  private fun observeSensor(type: Int): Flowable<SensorEvent> = Flowable.create({ emitter ->

    val sensor = sensorManager.getDefaultSensor(type)

    if (sensor == null) {
      emitter.onError(Throwable("Sensors not available for type $type"))
      return@create
    }

    sensorManager
      .registerListener(object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) =
          emitter.onNext(event)

        override fun onAccuracyChanged(sensor: Sensor, point: Int) {
          /* No op */
        }

      }, sensor, SENSOR_DELAY_GAME)
  }, BackpressureStrategy.BUFFER)
}