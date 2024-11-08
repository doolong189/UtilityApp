package com.freshervnc.utilityapplication.ui.pdfviewer

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.DialogChooseConvertPdfBinding
import com.freshervnc.utilityapplication.databinding.DialogCreatePdfBinding
import com.freshervnc.utilityapplication.databinding.FragmentPdfViewerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class PdfViewerFragment : Fragment() {
    private lateinit var binding: FragmentPdfViewerBinding
    private var PICK_IMAGE_MULTIPLE = 1
    private val PICK_PDF_FILE: Int = 2
    private val REQUEST_IMAGE_CAPTURE = 1
    private var dialog: BottomSheetDialog? = null
    private var mArrayUri = ArrayList<Uri>()
    private var position = 0
    private lateinit var document: Uri
    lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPdfViewerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        action()
//        getData()
    }

    private fun action() {
        binding.pdfViewerFlBtn.setOnClickListener {
            val view = DialogChooseConvertPdfBinding.inflate(layoutInflater, null, false)
            dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
            dialog?.setContentView(view.root)
            dialog?.show()
            //action
            view.dialogPdfBtnConvertImage.setOnClickListener {
                selectImage()
            }
            view.dialogPdfBtnConvertWord.setOnClickListener {
                selectWord()
            }
            view.dialogPdfBtnConvertScanQR.setOnClickListener {
                selectCameraQrScan()
            }
        }
    }

    private fun selectImage() {
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_MULTIPLE
            )
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
        }
    }

    private fun selectWord() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // mime types for MS Word documents
        val mimetypes = arrayOf(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
        )
        intent.setType("*/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        startActivityForResult(intent, PICK_PDF_FILE)
    }

    private fun selectCameraQrScan() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.freshervnc.utilityapplication.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bitmaps = mutableListOf<Bitmap>()
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
            if (data.clipData != null) {
                val count: Int = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUrl: Uri = data.clipData!!.getItemAt(i).uri
                    mArrayUri.add(imageUrl)
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        imageUrl
                    )
                    bitmaps.add(bitmap)
                }
                binding.outputImage.setImageURI(mArrayUri[0])
                position = 0
            } else {
                val imageUrl: Uri = data.data!!
                mArrayUri.add(imageUrl)
                binding.outputImage.setImageURI(mArrayUri[0])
                position = 0
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUrl)
                bitmaps.add(bitmap)
            }
            binding.pdfViewerFlConvert.visibility = View.VISIBLE
            binding.pdfViewerFlConvert.setOnClickListener {
                convertImageToPdf(bitmaps)
            }

            dialog?.dismiss()
        } else {
            Toast.makeText(requireContext(), "You haven't picked Image", Toast.LENGTH_LONG).show()
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras!!.get("data") as Bitmap
            binding.outputImage.setImageBitmap(imageBitmap)
        }else {
            Toast.makeText(requireContext(), "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }

    private fun createPdf(bitmaps: MutableList<Bitmap>, filePath: String) {
        if (bitmaps.isEmpty()) {
            Toast.makeText(requireContext(), "No images selected", Toast.LENGTH_SHORT).show()
            return
        }
        val pdfDocument = PdfDocument()
        try {
            bitmaps.forEachIndexed { index, bitmap ->
                // Tạo PageInfo cho từng trang
                val pageInfo =
                    PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                // Vẽ bitmap trên canvas của trang hiện tại
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)
            }
            // Lưu PDF
            val pdfFile = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                filePath + ".pdf"
            )
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Toast.makeText(
                requireContext(),
                "PDF saved successfully to ${pdfFile.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("PDF Creation", "PDF saved successfully to ${pdfFile.absolutePath}")
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(requireContext(), "PDF save failed: ${ex.message}", Toast.LENGTH_SHORT)
                .show()
            Log.e("PDF Creation", "PDF save failed: ${ex.message}")
        } finally {
            pdfDocument.close()
        }
    }

    //get All image in gallery
    fun getData() {
        val allImages = getAllImagesFromGallery(requireContext())
        Log.e("zzz o day", "" + allImages.size)
        binding.outputImage.setImageURI(allImages[0])
        position = 0
    }

    private fun getAllImagesFromGallery(context: Context): List<Uri> {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val imageUri = Uri.withAppendedPath(uri, id.toString())
                mArrayUri.add(imageUri)
            }
        }
        return mArrayUri
    }

    private fun convertImageToPdf(bitmaps: MutableList<Bitmap>) {
        val view = DialogCreatePdfBinding.inflate(layoutInflater, null, false)
        val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(view.root)
        dialog.show()
        val random = (0..100000).shuffled().last()
        view.dialogCreatePdfEdFileName.setText("Utility_random_$random")
        view.dialogCreatePdfBtnConvert.setOnClickListener {
            createPdf(bitmaps, view.dialogCreatePdfEdFileName.text.toString())
            dialog.dismiss()
        }
        view.dialogCreatePdfSwPassword.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                view.dialogCreatePdfEdPassword.visibility = View.VISIBLE
            } else {
                view.dialogCreatePdfEdPassword.visibility = View.GONE
            }
        })
    }

    private fun convertWordToPdf() {

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}