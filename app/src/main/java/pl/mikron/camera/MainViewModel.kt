package pl.mikron.camera

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pl.mikron.camera.media.MediaPlayerWrapper
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val player: MediaPlayerWrapper
) : ViewModel() {
  private val disposables = CompositeDisposable()

  override fun onCleared() {
    super.onCleared()
    disposables.dispose()
  }

  private val _position: MutableLiveData<Int> =
    MutableLiveData(0)

  val currentPosition: LiveData<Int> =
    _position

  val position: LiveData<String> =
    _position.map {
      String.format("%02d:%02d", it / 60000, (it / 1000) % 60)
    }

  private val _duration: MutableLiveData<Int> =
    MutableLiveData(0)

  val duration: LiveData<String> =
    _duration.map {
      String.format("%02d:%02d", it / 60000, (it / 1000) % 60)
    }

  private val _title: MutableLiveData<String> =
    MutableLiveData("Select file or start recording")

  val title: LiveData<String> =
    _title

  private var positionDisposable: Disposable? = null

  internal fun observeSongData() {
    positionDisposable?.dispose()

    positionDisposable =
      player
        .observePosition()
        .subscribeOn(Schedulers.computation())
        .subscribe(_position::postValue)
        .also { disposables.add(it) }

    _title.postValue(player.getArtist())
    _duration.postValue(player.getDuration())
  }

  internal fun stopSongDataObservation() {
    positionDisposable?.dispose()
  }

  internal fun setupForRecording() {
    stopSongDataObservation()
    _position.postValue(0)
    _duration.postValue(0)
    _title.postValue("Nagrywanie")
  }

  private val _recording: MutableLiveData<Boolean> =
    MutableLiveData(false)

  val recording: LiveData<Boolean> =
    _recording

  internal fun setRecording(recording: Boolean) =
    _recording.postValue(recording)

  private val _playbackEvent: MutableLiveData<PlaybackEvent> =
    MutableLiveData()

  val playbackEvent: LiveData<PlaybackEvent> =
    _playbackEvent

  fun onPlayClicked() =
    _playbackEvent.postValue(PlaybackEvent.Play)

  fun onPauseClicked() =
    _playbackEvent.postValue(PlaybackEvent.Pause)

  fun onStopClicked() =
    _playbackEvent.postValue(PlaybackEvent.Stop)
}
