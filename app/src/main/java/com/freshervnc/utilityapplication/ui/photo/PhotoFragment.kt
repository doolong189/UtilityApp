package com.freshervnc.utilityapplication.ui.photo


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.DialogAddStickerBinding
import com.freshervnc.utilityapplication.databinding.DialogAddTextBinding
import com.freshervnc.utilityapplication.databinding.DialogColorPickerBinding
import com.freshervnc.utilityapplication.databinding.FragmentPhotoBinding
import com.freshervnc.utilityapplication.ui.photo.adapter.ColorPickerAdapter
import com.freshervnc.utilityapplication.ui.photo.adapter.ColorPickerListener
import com.freshervnc.utilityapplication.ui.photo.adapter.ColorTextAdapter
import com.freshervnc.utilityapplication.ui.photo.adapter.ColorTextListener
import com.freshervnc.utilityapplication.ui.photo.adapter.StickerAdapter
import com.freshervnc.utilityapplication.ui.photo.adapter.StickerListener
import com.freshervnc.utilityapplication.ui.photo.draw.MyDrawView
import com.freshervnc.utilityapplication.ui.photo.sticker.StickerImageView
import com.freshervnc.utilityapplication.ui.photo.sticker.StickerTextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PhotoFragment : Fragment(), StickerListener, ColorTextListener, ColorPickerListener {
    private val CROP_IMAGE = 1
    private val LOAD_IMAGE_GALLARY = 2
    private lateinit var binding: FragmentPhotoBinding
    private val listEmojiIcon: MutableList<Int> =
        arrayListOf(R.drawable.icon_emoji,R.drawable.angry,R.drawable.dizzy,R.drawable.emoji,R.drawable.flame, R.drawable.kiss , R.drawable.quiet)
    private val listColor: MutableList<String> = arrayListOf("#FF0000","#FFFFFF","#FCBA03","#2D8707",
        "#1ABA95","#0E5B9E","#080BBF","#9602F2","#F20246","#FF0008","#423D3D","#D6D6D6")
    private var colorText: String = "#FF0000"
    private var colorPicker: String = "#FF0000"
    private var colorSize: Float = 3f
    private var dialog: BottomSheetDialog? = null
    private var dialogSticker: BottomSheetDialog? = null
    private var dialogColorPicker: BottomSheetDialog? = null
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        onPermission()
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
                    imageUri = uri
                    binding.photoImageView.setImageURI(uri)
                    val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        it.moveToFirst()
                        val fileSize = it.getLong(sizeIndex)
                        val filePath = uri.path ?: "Không lấy được đường dẫn"
                        binding.photoTvFilePath.text = "Đường dẫn: $filePath"

                        binding.photoImgInfoFilePath.setOnClickListener {
                            getDetailInfoImage(filePath,fileSize)
                        }
                    }
                    binding.photoBtnSelectImage.visibility = View.GONE
                    binding.photoLayOutButton.visibility = View.VISIBLE
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
        binding.editEmoticon.setOnClickListener {
            addSticker()
        }

        binding.editChangeBackground.setOnClickListener {
            getImageColorWhiteBlack()
        }

        binding.editPaintDraw.setOnClickListener {
            getDrawPaint()
        }

        binding.editSaveImage.setOnClickListener {
            saveImage()
        }
    }

    private fun getDetailInfoImage(filePath: String, fileSize: Long){
        val mAlertDialog = AlertDialog.Builder(requireContext())
        mAlertDialog.setMessage("Đường dẫn: $filePath\nKích thước: ${fileSize / 1024} KB")
        mAlertDialog.setPositiveButton("OK") { dialog, id ->
            dialog.dismiss()
        }
        mAlertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOAD_IMAGE_GALLARY && resultCode == RESULT_OK) {
            cropImage(data?.data)
        } else if (requestCode == LOAD_IMAGE_GALLARY) {
            if (data != null) {
                cropImage(data.data)
            }
        } else if (requestCode == CROP_IMAGE) {
            if (data != null) {
                val extras = data.extras
                val photo = extras!!.getParcelable<Bitmap>("data")
                binding.photoImageView.setImageBitmap(photo)
            }
        }
    }


    private fun addSticker() {
        val view = DialogAddStickerBinding.inflate(layoutInflater, null, false)
        dialogSticker = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        dialogSticker?.setContentView(view.root)
        dialogSticker?.show()
        dialogSticker?.setCancelable(false)
        val adapter = StickerAdapter(requireContext(), listEmojiIcon, this)
        binding.photoLayOutButton.visibility = View.GONE
        view.grView.adapter = adapter
    }
    private fun addText() {
        val view = DialogAddTextBinding.inflate(layoutInflater, null, false)
        dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        dialog?.setContentView(view.root)
        dialog?.show()
        dialog?.setCancelable(false)
        val adapter = ColorTextAdapter(listColor, this)
        view.rcListColor.adapter = adapter
        val tvSticker = StickerTextView(requireContext())
        view.dialogTextBtnSave.setOnClickListener {
            tvSticker.setText(view.dialogTextEdText.text.toString())
            tvSticker.setTextColor(colorText)
            binding.canvas.addView(tvSticker)
            dialog?.dismiss()
        }
        view.dialogTextBtnExit.setOnClickListener {
            dialog?.dismiss()
        }
    }
    @SuppressLint("NewApi")
    private fun getImageColorWhiteBlack() {
        val source = ImageDecoder.createSource(requireActivity().contentResolver, imageUri!!)
        val bitmap = ImageDecoder.decodeBitmap(source)
        val drawable = BitmapDrawable(resources, bitmap)
        // Tạo ColorMatrix cho hiệu ứng đen trắng
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        drawable.colorFilter = filter
        binding.photoImageView.setImageDrawable(drawable)
    }
    private fun getDrawPaint() {
        getShowSettingDraw()
        binding.photoLayoutColor.visibility = View.VISIBLE
        binding.photoLayOutButton.visibility = View.GONE
    }
    private fun getShowSettingDraw() {
        var myDrawView = MyDrawView(requireContext(), Color.parseColor(colorPicker), colorSize)
        myDrawView.clear()
        binding.canvas.addView(myDrawView)
        binding.photoIconShowHide.setOnClickListener {
            binding.photoLayoutColor.visibility = View.GONE
            binding.photoLayOutButton.visibility = View.VISIBLE
        }
        binding.photoIconPickColor.setOnClickListener {
            myDrawView.clear()
            dialogColorPicker()
        }
        binding.photoIconReverse.setOnClickListener {
            myDrawView.clear()
            getShowSettingDraw()
        }
        binding.photoIconDelete.setOnClickListener {
            binding.canvas.removeView(myDrawView)
            myDrawView = MyDrawView(requireContext(), Color.parseColor(colorPicker), colorSize)
            binding.canvas.addView(myDrawView)
        }
    }
    override fun onClickItem(position: Int) {
        val ivSticker = StickerImageView(requireContext())
        ivSticker.setImageResource(position)
        binding.canvas.addView(ivSticker)
        binding.photoLayOutButton.visibility = View.VISIBLE
        dialogSticker?.dismiss()
    }

    override fun onClickItemColorText(position: String , position1: Int) {
        colorText = position
    }

    private fun dialogColorPicker() {
        val view = DialogColorPickerBinding.inflate(layoutInflater, null, false)
        dialogColorPicker = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        dialogColorPicker?.setContentView(view.root)
        dialogColorPicker?.show()
        dialogColorPicker?.setCancelable(false)
        val manager = GridLayoutManager(requireContext(), 4)
        view.rcListColor.layoutManager = manager
        val adapter = ColorPickerAdapter(listColor, this)
        view.rcListColor.adapter = adapter
    }

    override fun onClickItemColorPicker(position: String) {
        colorPicker = position
        val myDrawView = MyDrawView(requireContext(), Color.parseColor(colorPicker), colorSize)
        binding.canvas.addView(myDrawView)
        dialogColorPicker?.dismiss()
        getShowSettingDraw()

    }

    private fun saveFrameLayoutAsImage(frameLayout: FrameLayout) {
        frameLayout.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(frameLayout.drawingCache)
        frameLayout.isDrawingCacheEnabled = false

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"

        // Check if running on an emulator
        val isEmulator = Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))

        // Choose storage directory based on device type
        val storageDir = if (isEmulator) {
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/wrapper" + "/data" + fileName)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        }

        val imageFile = File(storageDir, fileName)
        try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(
                frameLayout.context,
                "Image saved to ${imageFile.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(frameLayout.context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPermission() {
        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) !=
            PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ),
                0
            )
        }
    }

    private fun cropImage(picUri: Uri?) {
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
            Log.e("zzzz", e.message.toString())
        }
    }

    private fun saveImage(){
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                LOAD_IMAGE_GALLARY
            )
        } else {
            saveFrameLayoutAsImage(binding.canvas)
        }
    }
}