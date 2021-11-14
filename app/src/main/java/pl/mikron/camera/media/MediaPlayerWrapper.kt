package pl.mikron.camera.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import pl.mikron.camera.PlaybackEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlayerWrapper @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private var mediaPlayer: MediaPlayer? = null
  private var currentUri: Uri? = null

  private fun init() {
    mediaPlayer = null
    mediaPlayer = MediaPlayer()
    mediaPlayer?.apply {
      setVolume(1f, 1f)
      setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      )
    }
  }

  fun updateUri(uri: Uri) = with(mediaPlayer) {
    if (this?.isPlaying == true) this.stop()
    this?.release()
    init()
    mediaPlayer?.apply {
      setDataSource(context.applicationContext, uri)
      currentUri= uri
      prepare()
    }
  }

  fun getArtist(): String =
    currentUri?.lastPathSegment.toString()

  fun getDuration() =
    mediaPlayer?.duration ?: 0

  fun observePosition(): Flowable<Int> =
    Flowable.create({ emitter ->
      while (true) {
        if (mediaPlayer == null) {
          emitter.onComplete()
          return@create
        } else {
          try {
            emitter.onNext(mediaPlayer?.currentPosition ?: 0)
          } catch (e: Exception) { }
        }
      }
    }, BackpressureStrategy.BUFFER)

  fun updatePosition(position: Int) =
    mediaPlayer?.seekTo(position)

  fun handlePlaybackEvent(event: PlaybackEvent) =
    when (event) {
      PlaybackEvent.Play -> mediaPlayer!!.start()
      PlaybackEvent.Pause -> if (mediaPlayer!!.isPlaying) mediaPlayer?.pause() else Unit
      PlaybackEvent.Stop -> with(mediaPlayer!!) {
        stop()
        prepare()
      }
    }
}
