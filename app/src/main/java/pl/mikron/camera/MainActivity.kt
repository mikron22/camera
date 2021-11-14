package pl.mikron.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import pl.mikron.camera.databinding.ActivityMainBinding
import pl.mikron.camera.extensions.ContextExt.CREATE_FILE_CODE
import pl.mikron.camera.extensions.ContextExt.OPEN_FILE_CODE
import pl.mikron.camera.extensions.createFile
import pl.mikron.camera.extensions.pickFile
import pl.mikron.camera.media.AudioMode
import pl.mikron.camera.media.MediaPlayerWrapper
import pl.mikron.camera.media.MediaRecorderWrapper
import javax.inject.Inject

@RuntimePermissions
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val disposables = CompositeDisposable()

  private val viewModel: MainViewModel
      by viewModels()

  @Inject
  lateinit var player: MediaPlayerWrapper

  @Inject
  lateinit var recorder: MediaRecorderWrapper

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.viewModel = viewModel
    binding.lifecycleOwner = this
    setUpMenu()
    setUpSlider()

    viewModel
      .playbackEvent
      .observe(this, ::handlePlaybackEvent)
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }

  override fun onActivityResult(
    requestCode: Int, resultCode: Int, resultData: Intent?
  ) {
    if (requestCode == OPEN_FILE_CODE && resultCode == Activity.RESULT_OK) {
      resultData?.data?.also { uri ->
        initPlayer(uri)
      }
    }
    if (requestCode == CREATE_FILE_CODE && resultCode == Activity.RESULT_OK) {
      resultData?.data?.also { uri ->
        initRecorderWithPermissionCheck(uri)
      }
    }
    super.onActivityResult(requestCode, resultCode, resultData)
  }

  private fun setUpMenu() {
    binding.toolbarContainer.inflateMenu(R.menu.main_menu)
    binding.toolbarContainer.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.openFile -> handleOpenFile()
        R.id.record -> handleRecord()
      }
      return@setOnMenuItemClickListener false
    }
  }

  private fun setUpSlider() {
    binding
      .progressView
      .addOnChangeListener { _, value, fromUser ->
        if (fromUser && audioMode == AudioMode.Player) {
          player.updatePosition(value.toInt())
        }
      }
  }

  private var audioMode: AudioMode = AudioMode.None

  private fun initPlayer(uri: Uri) {
    player.updateUri(uri)
    binding.progressView.valueTo = player.getDuration().toFloat()
    viewModel.observeSongData()
  }

  private fun handleOpenFile() {
    recordingInProgress(false)
    binding.cdView.clearAnimation()
    audioMode = AudioMode.Player
    viewModel.stopSongDataObservation()
    pickFile()
  }

  private fun handleRecord() {
    recordingInProgress(false)
    binding.cdView.clearAnimation()
    audioMode = AudioMode.Recorder
    viewModel.setupForRecording()
    createFile()
  }

  private fun handlePlaybackEvent(event: PlaybackEvent) {
    when (audioMode) {
      AudioMode.Player -> handlePlayerEvent(event)
      AudioMode.Recorder -> handleRecorderEvent(event)
      AudioMode.None -> Snackbar.make(
        binding.rootView,
        "Wybierz plik lub zacznij nagrywanie",
        LENGTH_SHORT
      ).show()
    }
  }

  private fun handlePlayerEvent(event: PlaybackEvent) {
    when (event) {
      PlaybackEvent.Play -> binding.cdView.startAnimation(
        AnimationUtils.loadAnimation(this, R.anim.rotation)
      )
      else -> binding.cdView.clearAnimation()
    }
    player.handlePlaybackEvent(event)
  }

  @NeedsPermission(Manifest.permission.RECORD_AUDIO)
  fun initRecorder(uri: Uri) {
    recorder.updateUri(uri)
  }

  private fun handleRecorderEvent(event: PlaybackEvent) {
    recordingInProgress(event == PlaybackEvent.Play)
    recorder.handlePlaybackEvent(event)
    if (event == PlaybackEvent.Stop) Snackbar.make(
      binding.rootView,
      "Nagranie zapisane",
      LENGTH_SHORT
    ).show()
  }

  private fun recordingInProgress(inProgress: Boolean) =
    viewModel.setRecording(inProgress)
}
