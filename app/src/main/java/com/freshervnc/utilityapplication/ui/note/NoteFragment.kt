package com.freshervnc.utilityapplication.ui.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.FragmentNoteBinding


class NoteFragment : Fragment() {
    private lateinit var binding : FragmentNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentNoteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun getData(){

    }
}