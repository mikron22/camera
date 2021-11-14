package pl.mikron.camera.media

sealed class AudioMode {
  object Player : AudioMode()
  object Recorder : AudioMode()
  object None : AudioMode()
}
