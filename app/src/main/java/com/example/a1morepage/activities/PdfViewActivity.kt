 package com.example.a1morepage.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.a1morepage.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

 class PdfViewActivity : AppCompatActivity() {

     private lateinit var binding: ActivityPdfViewBinding

     // TAG
     private companion object {
         const val TAG = "PDF_VIEW_TAG"
     }
     //book id
     private var bookId = ""

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityPdfViewBinding.inflate(layoutInflater)
         setContentView(binding.root)

         //get book id from intent
         bookId = intent.getStringExtra("bookId") ?: ""
         if (bookId.isEmpty()) {
             Log.e(TAG, "No bookId provided in intent")
             finish()
             return
         }
         loadBookDetails()

         //handle click, goback
         binding.backBtn.setOnClickListener {
             onBackPressed()
         }
     }

     private fun extractRelativePathFromUrl(fullUrl: String): String {
         val regex = """o\/(.*?)\?""".toRegex()
         val matchResult = regex.find(fullUrl)
         return matchResult?.groupValues?.get(1)?.replace("%2F", "/") ?: ""
     }

     private fun loadBookDetails() {
         Log.d(TAG, "loadBookDetails: Get PDF URL from db")

         val ref = FirebaseDatabase.getInstance().getReference("Books")
         ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 // get book full URL
                 val pdfFullUrl = snapshot.child("url").value.toString()
                 Log.d(TAG, "onDataChange: PDF_Full_URL: $pdfFullUrl")

                 // extract relative path from the full URL
                 val relativePath = extractRelativePathFromUrl(pdfFullUrl)
                 Log.d(TAG, "onDataChange: PDF_Relative_Path: $relativePath")

                 // load pdf using the extracted relative path
                 loadBookFromUrl(relativePath)
             }

             override fun onCancelled(error: DatabaseError) {}
         })
     }

     private fun loadBookFromUrl(pdfUrl: String) {
         Log.d(TAG, "loadBookFromUrl: Get PDF from firebase storage using URL")
         val reference = FirebaseStorage.getInstance().getReference(pdfUrl)
         reference.getBytes(com.example.a1morepage.Constants.MAX_BYTES_PDF)
             .addOnSuccessListener { bytes ->
                 // load pdf
                 binding.pdfView.fromBytes(bytes)
                     .swipeHorizontal(false)
                     .onPageChange { page, pageCount ->
                         //set current and total pages in toolbar subtitle
                         val currentPage =
                             page + 1 //page starts from 0, that's why I'm adding 1 for non-programmers to not confuse them :P
                         binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                         Log.d(TAG, "loadBookFromUrl:$currentPage/$pageCount")
                     }

                     .onPageError { page, t ->
                         Log.e(TAG, "Error on page $page: ${t.message}")
                     }
                     .load()
                 binding.progressBar.visibility = View.GONE
             }
             .addOnFailureListener { e ->
                 Log.e(TAG, "Failed to get pdf: ${e.message}")
                 binding.progressBar.visibility = View.GONE
             }
     }
 }
