package com.freshervnc.utilityapplication.ui.pdfviewer

import android.app.Activity.FOCUSED_STATE_SET
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.DialogChooseConvertPdfBinding
import com.freshervnc.utilityapplication.databinding.FragmentPdfViewerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.FileOutputStream


class PdfViewerFragment : Fragment() {
    private lateinit var binding: FragmentPdfViewerBinding
    private var PICK_IMAGE_MULTIPLE = 1
    private var dialog: BottomSheetDialog? = null
    private var mArrayUri = ArrayList<Uri>()
    private var position = 0
    private val PICK_PDF_FILE: Int = 2
    private lateinit var document : Uri

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
        }

        binding.btnNext.setOnClickListener {
            if (position < mArrayUri.size - 1) {
                position++
                binding.outputImage.setImageURI(mArrayUri[position])
            } else {
                Toast.makeText(requireContext(), "Last Image Already Shown", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnPrevious.setOnClickListener {
            if (position > 0) {
                position--
                binding.outputImage.setImageURI(mArrayUri[position])
            } else {
                Toast.makeText(requireContext(), "First Image Already Shown", Toast.LENGTH_SHORT)
                    .show()
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
                Intent.createChooser(intent, "Select Picture")
                , PICK_IMAGE_MULTIPLE
            )
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bitmaps = mutableListOf<Bitmap>()
        if (requestCode === PICK_IMAGE_MULTIPLE && resultCode === RESULT_OK && null != data) {
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
            binding.btnImageToPdf.setOnClickListener {
                createPdf(bitmaps)
            }
        } else {
            Toast.makeText(requireContext(), "You haven't picked Image", Toast.LENGTH_LONG).show()
        }

    }

    private fun createPdf(bitmaps: MutableList<Bitmap>) {
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
                "pdf_image.pdf"
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
    fun getData(){
        val allImages = getAllImagesFromGallery(requireContext())
        Log.e("zzz o day",""+allImages.size)
        binding.outputImage.setImageURI(allImages[0])
        position = 0
    }
    fun getAllImagesFromGallery(context: Context): List<Uri> {
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

}