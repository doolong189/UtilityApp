package com.freshervnc.utilityapplication.ui.photo

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.DialogAddTextBinding
import com.freshervnc.utilityapplication.databinding.FragmentPhotoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


class PhotoFragment : Fragment() {
    private lateinit var binding: FragmentPhotoBinding
    private val CROP_IMAGE = 1
    private val LOAD_IMAGE_GALLARY = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        action()
    }

    private fun action() {
        val singlePhotoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    binding.photoImageView.setImageURI(uri)
                    val cursor =
                        requireActivity().contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        it.moveToFirst()
                        val fileSize = it.getLong(sizeIndex)
                        val filePath = uri.path ?: "Không lấy được đường dẫn"
                        binding.photoTvFilePath.text = "Đường dẫn: $filePath"
                        binding.photoImgInfoFilePath.setOnClickListener {
                            val mAlertDialog = AlertDialog.Builder(requireContext())
                            mAlertDialog.setMessage("Đường dẫn: $filePath\nKích thước: ${fileSize / 1024} KB")
                            mAlertDialog.setPositiveButton("OK") { dialog, id ->
                                dialog.dismiss()
                            }
                            mAlertDialog.show()
                        }
                    }
                    binding.photoBtnSelectImage.visibility = View.GONE
                    binding.photoLayOutButton.visibility = View.VISIBLE
                    binding.editCropImage.setOnClickListener {
                        CropImage(uri)
                    }
                }
            }

        binding.photoBtnSelectImage.setOnClickListener {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        binding.editAddText.setOnClickListener {
            addText()
        }
    }

    private fun CropImage(picUri : Uri?) {
        try {
            val intent = Intent("com.android.camera.action.CROP")
            intent.setDataAndType(picUri, "image/*")
            intent.putExtra("crop", "true")
            intent.putExtra("outputX", 250)
            intent.putExtra("outputY", 250)
            intent.putExtra("aspectX", 3)
            intent.putExtra("aspectY", 4)
            intent.putExtra("scaleUpIfNeeded", true)
            intent.putExtra("return-data", true)
            startActivityForResult(intent, CROP_IMAGE)
        } catch (e: ActivityNotFoundException) {
            Log.e("zzzz",e.message.toString())
        }
    }

    private fun AddSticker(){

    }

    private fun addText(){
        val view = DialogAddTextBinding.inflate(layoutInflater, null, false)
        val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(view.root)
        dialog.show()
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOAD_IMAGE_GALLARY && resultCode == RESULT_OK) {
            CropImage(data?.data)
        } else if (requestCode == LOAD_IMAGE_GALLARY) {
            if (data != null) {
                CropImage(data.data)
            }
        } else if (requestCode == CROP_IMAGE) {
            if (data != null) {
                // get the returned data
                val extras = data.extras
                // get the cropped bitmap
                val photo = extras!!.getParcelable<Bitmap>("data")
                binding.photoImageView.setImageBitmap(photo)
            }
        }
    }
}