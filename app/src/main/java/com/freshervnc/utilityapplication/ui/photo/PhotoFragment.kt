package com.freshervnc.utilityapplication.ui.photo


import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import androidx.fragment.app.Fragment
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.DialogAddStickerBinding
import com.freshervnc.utilityapplication.databinding.DialogAddTextBinding
import com.freshervnc.utilityapplication.databinding.FragmentPhotoBinding
import com.freshervnc.utilityapplication.ui.photo.adapter.ColorAdapter
import com.freshervnc.utilityapplication.ui.photo.adapter.ColorListener
import com.freshervnc.utilityapplication.ui.photo.adapter.StickerAdapter
import com.freshervnc.utilityapplication.ui.photo.adapter.StickerListener
import com.freshervnc.utilityapplication.ui.photo.draw.MyDrawView
import com.freshervnc.utilityapplication.ui.photo.sticker.StickerImageView
import com.freshervnc.utilityapplication.ui.photo.sticker.StickerTextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.FileOutputStream


class PhotoFragment : Fragment(), StickerListener, ColorListener {
    private val CROP_IMAGE = 1
    private val LOAD_IMAGE_GALLARY = 2
    private lateinit var binding: FragmentPhotoBinding
    private val listEmojiIcon: MutableList<Int> = mutableListOf()
    private val listColor: MutableList<String> = mutableListOf()
    private var colorStr : String = ""
    private var dialog: BottomSheetDialog? = null
    private var imageUri : Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                    imageUri = uri
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
                        cropImage(uri)
                    }
                    saveImageToGallery(uri)
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
            binding.photoLayoutColor.visibility = View.VISIBLE
            binding.photoLayOutButton.visibility = View.GONE
        }

        binding.editBlurImage.setOnClickListener {

        }

        binding.editDeleteImage.setOnClickListener {
            binding.canvas.isDrawingCacheEnabled = true
            binding.canvas.buildDrawingCache()
            val bitmap = binding.canvas.drawingCache
            try {
                val rootFile = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "MyImage" + File.separator
                )
                rootFile.mkdirs()
                val fileOutputStream = FileOutputStream(rootFile)
                bitmap.compress(CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                Toast.makeText(requireContext(),"Luu anh thanh cong",Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
            }
        }
    }

    private fun cropImage(picUri : Uri?) {
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
    override fun onClickItem(position: Int) {
        val ivSticker = StickerImageView(requireContext())
        ivSticker.setImageResource(position)
        binding.canvas.addView(ivSticker)
        binding.photoLayOutButton.visibility = View.VISIBLE
        dialog ?.dismiss()
        listEmojiIcon  ?.clear()
    }

    /* sticker emojicon */
    private fun getListEmojicon(){
        listEmojiIcon ?.add(R.drawable.icon_emoji)
        listEmojiIcon ?.add(R.drawable.angry)
        listEmojiIcon ?.add(R.drawable.dizzy)
        listEmojiIcon ?.add(R.drawable.emoji)
        listEmojiIcon ?.add(R.drawable.flame)
        listEmojiIcon ?.add(R.drawable.kiss)
        listEmojiIcon ?.add(R.drawable.quiet)
    }
    private fun addSticker(){
        val view = DialogAddStickerBinding.inflate(layoutInflater, null, false)
        dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        dialog ?.setContentView(view.root)
        dialog ?.show()
        getListEmojicon()
        view.grView.visibility = View.VISIBLE
        binding.photoLayOutButton.visibility = View.GONE
        val adapter = StickerAdapter(requireContext(), listEmojiIcon!! , this)
        view.grView.adapter = adapter
    }

    /* add text */
    private fun getListColor(){
        listColor ?.add("#FF000000")
        listColor ?.add("#FFFFFFFF")
        listColor ?.add("#fcba03")
        listColor ?.add("#2d8707")
        listColor ?.add("#1aba95")
        listColor ?.add("#0e5b9e")
        listColor ?.add("#080bbf")
        listColor ?.add("#9602f2")
        listColor ?.add("#f20246")
        listColor ?.add("#ff0008")
        listColor ?.add("#423d3d")
        listColor ?.add("#d6d6d6")
    }
    private fun addText(){
        val view = DialogAddTextBinding.inflate(layoutInflater, null, false)
        dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        dialog ?.setContentView(view.root)
        dialog ?.show()
        getListColor()
        val adapter = ColorAdapter(listColor!! , this)
        view.rcListColor.adapter = adapter
        val tvSticker = StickerTextView(requireContext())
        tvSticker.setTextColor("#000000")
        view.dialogTextBtnSave.setOnClickListener {
            tvSticker.setText(view.dialogTextEdText.text.toString())
            tvSticker.setTextColor(colorStr)
            binding.canvas.addView(tvSticker)
            dialog ?.dismiss()
        }
    }
    override fun onClickItem(position: String) {
        colorStr = position
    }

    /* change image background black-white */
    private fun getImageColorWhiteBlack(){
        // Tạo BitmapDrawable từ hình ảnh gốc
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

    /* draw paint overlay image */
    private fun getDrawPaint(){
        val myDrawView = MyDrawView(requireContext())
        binding.canvas.addView(myDrawView)
    }


    private fun saveImageToGallery(imageUri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "my_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppImages")
        }

        val resolver = requireActivity().contentResolver
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val newImageUri = resolver.insert(imageCollection, contentValues)

        newImageUri?.let { outputStreamUri ->
            resolver.openOutputStream(outputStreamUri)?.use { outputStream ->
                resolver.openInputStream(imageUri)?.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}