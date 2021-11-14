package pl.mikron.camera.media

import android.content.Context
import android.media.AudioFormat
import android.media.MediaRecorder
import android.net.Uri
import pl.mikron.camera.PlaybackEvent
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileDescriptor


@Singleton
class MediaRecorderWrapper @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private var mediaRecorder: MediaRecorder? = null
  private var started = false

  private fun init() {
    mediaRecorder = null
    mediaRecorder = MediaRecorder()
    mediaRecorder?.apply {
      setAudioSource(MediaRecorder.AudioSource.MIC)
      setOutputFormat(AudioFormat.ENCODING_PCM_16BIT)
      setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      setAudioChannels(1)
      setAudioEncodingBitRate(128000)
      setAudioSamplingRate(48000)
    }
  }

  fun updateUri(uri: Uri) {
    mediaRecorder?.release()
    init()
    mediaRecorder?.setOutputFile(getFileDescriptor(uri))
    mediaRecorder?.prepare()
    started = false
  }

  private fun startRecording() {
    mediaRecorder?.start()
    started = true
  }

  private fun pauseRecording() {
    mediaRecorder?.pause()
  }

  private fun resumeRecording() {
    mediaRecorder?.resume()
  }

  private fun stopRecording() {
    mediaRecorder?.stop()
    mediaRecorder?.release()
    mediaRecorder = null
    init()
  }

  fun handlePlaybackEvent(event: PlaybackEvent) =
    when (event) {
      PlaybackEvent.Play -> if (started) resumeRecording() else startRecording()
      PlaybackEvent.Pause -> pauseRecording()
      PlaybackEvent.Stop -> stopRecording()
    }

  private fun getFileDescriptor(uri: Uri): FileDescriptor? =
    context.contentResolver.openFileDescriptor(uri, "rw")?.fileDescriptor

}
