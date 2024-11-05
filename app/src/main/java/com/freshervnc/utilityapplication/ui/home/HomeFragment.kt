package com.freshervnc.utilityapplication.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.freshervnc.utilityapplication.MainActivity
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.FragmentHomeBinding
import com.freshervnc.utilityapplication.ui.photo.PhotoFragment
import com.freshervnc.utilityapplication.ui.pdfviewer.PdfViewerFragment
import com.freshervnc.utilityapplication.ui.video.EditVideoFragment
import com.google.android.material.snackbar.Snackbar


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        action()
    }

    private fun action() {
        binding.homeEditImage.setOnClickListener {
            (activity as MainActivity).replaceFragment(PhotoFragment())
        }
        binding.homeEditVideo.setOnClickListener {
            (activity as MainActivity).replaceFragment(EditVideoFragment())
        }
        binding.homePdfViewer.setOnClickListener {
            (activity as MainActivity).replaceFragment(PdfViewerFragment())
        }

    }


}