package com.freshervnc.utilityapplication.ui.video

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.ChannelMixingAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.FragmentEditVideoBinding
import com.freshervnc.utilityapplication.utils.Utils.FilePath
import com.google.common.collect.ImmutableList

class EditVideoFragment : Fragment() {
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentEditVideoBinding
    private lateinit var uriTemp: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditVideoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        action()
    }

    @OptIn(UnstableApi::class)
    private fun action() {
        val mediaController = MediaController(requireActivity())
        binding.videoVideoView.setMediaController(mediaController)
        mediaController.setAnchorView(binding.videoVideoView)

        val singleVideoLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                binding.videoVideoView.setVideoURI(uri)
                binding.videoVideoView.start()
                binding.videoLayoutOptions.visibility = View.VISIBLE
                binding.videoBtnSelectVideo.visibility = View.GONE
                binding.videoPgLoading.visibility = View.GONE
                uriTemp = uri!!
            }
        binding.videoBtnSelectVideo.setOnClickListener {
            singleVideoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
            binding.videoPgLoading.visibility = View.GONE
        }

        binding.photoIconCut.setOnClickListener {
            val trimVideoIntent = Intent("com.android.camera.action.TRIM")
            trimVideoIntent.putExtra("media-item-path", FilePath)
            trimVideoIntent.setData(uriTemp)
            val list: List<ResolveInfo> =
                requireActivity().packageManager.queryIntentActivities(trimVideoIntent, 0)
            if (list.isNotEmpty()) {
                startActivity(trimVideoIntent)
            } else {
                Toast.makeText(requireContext(), "not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadVideo() {
        binding.videoVideoView.setVideoURI(Uri.parse("android.resource://" + requireActivity().packageName + "/" + R.raw.video))
        val mediaController = MediaController(requireContext())
        binding.videoVideoView.setMediaController(mediaController)
        mediaController.setAnchorView(binding.videoVideoView)
        binding.videoPgLoading.visibility = View.GONE
    }
}