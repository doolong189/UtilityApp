package com.freshervnc.utilityapplication.ui.video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.FragmentEditVideoBinding


class EditVideoFragment : Fragment() {
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentEditVideoBinding
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
        loadVideo()
    }

    private fun action() {
        val mediaController = MediaController(requireActivity())
        binding.videoVideoView.setMediaController(mediaController)
        mediaController.setAnchorView(binding.videoVideoView)

        val singleVideoLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                binding.videoVideoView.setVideoURI(uri)
                binding.videoVideoView.start()
            }
        binding.videoBtnSelectVideo.setOnClickListener {
            singleVideoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }
    }

    private fun loadVideo() {
        binding.videoVideoView.setVideoURI(Uri.parse("android.resource://" + requireActivity().packageName + "/" + R.raw.video))
        // binding.videoVideoView.setVideoPath("https://github.com/Coding-Meet/Change-Theme-And-Font-App/raw/master/video/video.mp4")
        val mediaController = MediaController(requireContext())
        binding.videoVideoView.setMediaController(mediaController)
        mediaController.setAnchorView(binding.videoVideoView)
        binding.videoPgLoading.visibility = View.GONE
    }
}