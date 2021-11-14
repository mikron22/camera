package pl.mikron.camera

sealed class PlaybackEvent {
  object Play : PlaybackEvent()
  object Pause : PlaybackEvent()
  object Stop : PlaybackEvent()
}
