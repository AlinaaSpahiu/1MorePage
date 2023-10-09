package com.example.a1morepage

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.a1morepage.activities.PdfDetailActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class Operations : Application() {

    private lateinit var database: FirebaseDatabase

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
    }

    companion object {

        private const val TAG = "OPERATIONS_TAG"

        // A static method to convert in normal date format the timestamp
        fun formatTimestamptToDate(timestamp: Long): String {
            return try {
                val cal = Calendar.getInstance(Locale.ENGLISH)
                cal.timeInMillis = timestamp.toLong()
                DateFormat.format("dd/MM/yyyy", cal).toString()
            } catch (e: NumberFormatException) {
                Log.e(TAG, "Error converting timestamp to date", e)
                ""
            }
        }

        // This function gets the size of pdf
        fun getPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"
            if (pdfUrl == null) {
                Log.e(TAG, "pdfUrl is null")
                return
            }

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    // Convert bytes to KB/MB
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    sizeTv.text = when {
                        mb >= 1 -> "${String.format("%.2f", mb)} MB"
                        kb > 1 -> "${String.format("%.2f", kb)} KB"
                        else -> "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }

        // ---
        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView?,
            progressBar: ProgressBar?,
            pagesTv: TextView?
        ) {
            if (pdfView == null || progressBar == null) {
                Log.e(TAG, "PDFView or ProgressBar is null")
                return
            }

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    // Ensure that the bytes retrieved are not null or empty
                    if (bytes == null || bytes.isEmpty()) {
                        Log.e(TAG, "No data retrieved from Firebase")
                        return@addOnSuccessListener
                    }

                    try {
                        // Set to pdfView
                        pdfView.fromBytes(bytes)
                            .pages(0)
                            .spacing(0)
                            .swipeHorizontal(false)
                            .enableSwipe(false)
                            .onError { t ->
                                progressBar.visibility = View.INVISIBLE
                                Log.e(TAG, "loadPdfFromUrlSinglePage: Error loading PDF", t)
                            }
                            .onPageError { page, t ->
                                progressBar.visibility = View.INVISIBLE
                                Log.e(TAG, "loadPdfFromUrlSinglePage: Error loading page $page", t)
                            }
                            .onLoad { nbPages ->
                                Log.d(TAG, "loadPdfFromUrlSinglePage: Pages $nbPages")
                                // PDF loaded, we can set page count
                                progressBar.visibility = View.INVISIBLE

                                // If pageTv param is not null set page numbers
                                pagesTv?.text = "$nbPages"
                            }
                            .load()
                    } catch (e: Exception) {
                        progressBar.visibility = View.INVISIBLE
                        Log.e(TAG, "Exception while loading PDF: ", e)
                    }

                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.INVISIBLE
                    Log.e(TAG, "loadPdfSize: Failed to get metadata", e)
                }
        }

//        fun loadPdfFromUrlSinglePage(
//            pdfUrl: String,
//            pdfTitle: String,
//            pdfView: PDFView,
//            progressBar: ProgressBar,
//            pagesTv: TextView?
//        ) {
//            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
//            ref.getBytes(Constants.MAX_BYTES_PDF)
//                .addOnSuccessListener { bytes ->
//                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")
//
//                    // Set to pdfView
//                    pdfView.fromBytes(bytes)
//                        .pages(0) // Show first page only
//                        .spacing(0)
//                        .swipeHorizontal(false)
//                        .enableSwipe(false)
//                        .onError { t ->
//                            progressBar.visibility = View.INVISIBLE
//                            Log.e(TAG, "loadPdfFromUrlSinglePage: Error loading PDF", t)
//                        }
//                        .onPageError { page, t ->
//                            progressBar.visibility = View.INVISIBLE
//                            Log.e(TAG, "loadPdfFromUrlSinglePage: Error loading page $page", t)
//                        }
//                        .onLoad { nbPages ->
//                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages $nbPages")
//                            // PDF loaded, we can set page count
//                            progressBar.visibility = View.INVISIBLE
//
//                            // If pageTv param is not null set page numbers
//                            pagesTv?.text = "$nbPages"
//                        }
//                        .load()
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG, "loadPdfSize: Failed to get metadata", e)
//                }
//        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            // Load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"

                        // Set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "loadCategory: Error loading category", error.toException())
                    }
                })
        }


        fun deleteBook(context: Context, bookId: String, bookUrl:String, bookTitle: String){

            val TAG = "DELETE_BOOK_TAG"
            Log.d(TAG, "deleteBook: deleting...")

            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting ${bookTitle}...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook:Deleted from storage...")
                    Log.d(TAG, "deleteBook:Deleted from db now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: Deleted from db too...")
                        }
                        .addOnFailureListener { e->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context, "Failed to delete from db due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener{e->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context, "Failed to delete from storage due to ${e.message}", Toast.LENGTH_SHORT).show()

                }

        }


        fun incrementBookViewCount(bookId: String){
            //get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if(viewsCount == "" || viewsCount == "null"){
                            viewsCount = "0";
                        }
                        //increment views
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


        fun clearInputFields(vararg editTexts: EditText) {
            editTexts.forEach { it.setText("") }
        }

        public fun removeFromFavorite(context: Context, bookId: String) {
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeFavorite: Removing from fav")

            val firebaseAuth = FirebaseAuth.getInstance()

            //database ref
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFavorite: Removed from fav")
                    Toast.makeText(context, "Removed from fav", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "removeFavorite: Failed to remove from fav due to ${e.message}")
                    Toast.makeText(context, "Failed to add to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}
