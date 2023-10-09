package com.example.a1morepage.activities


import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


import com.example.a1morepage.Operations
import com.example.a1morepage.R
import com.example.a1morepage.databinding.ActivityPdfDetailBinding
import com.example.a1morepage.adapters.AdapterComment
import com.example.a1morepage.adapters.AdapterUsersFavorite
import com.example.a1morepage.databinding.DialogCommentAddBinding
import com.example.a1morepage.models.ModelComment
import com.example.a1morepage.models.ModelUser
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfDetailBinding

    private companion object{
        // TAG
        const val TAG = "BOOK_DETAILS_TAG"
    }

    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""
    private var isInMyFavorite = false

    private  lateinit var firebaseAuth : FirebaseAuth

    private lateinit var progressDialog : ProgressDialog

    private lateinit var usersFavoriteArrayList : ArrayList<ModelComment>
    private lateinit var usersFavoriteBookArrayList : ArrayList<ModelUser>

    private lateinit var adapterComment : AdapterComment
    private lateinit var adapterUser : AdapterUsersFavorite


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //progressbar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            checkIsFavorite()
        }

        Operations.incrementBookViewCount(bookId)
        loadBookDetails()
        showFavoriteUser()
        showComments()

        binding.favoriteBtn.setOnClickListener {
            checkIsFavorite()
        }

        binding.showFavoriteUsersBtn.setOnClickListener {
            fetchUsersAndShowDialog()
        }

        // Go back btn
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // Read book btn
        binding.readBookBtn.setOnClickListener{
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId);
            startActivity(Intent(intent))
        }

        // Add to favorites btn
        binding.favoriteBtn.setOnClickListener{
            // check if user is logged in
            if(firebaseAuth.currentUser == null){
                // user not logged in
                Toast.makeText(this, "You're not logged in!", Toast.LENGTH_SHORT).show()
            }
            else {
                // user is logged in
                if(isInMyFavorite){
                    // already in favorite, remove
                    Operations.removeFromFavorite(this, bookId)
                }else {
                    // not in favorite, add it
                    addToFavorite()
                }
            }
        }

        // add Comment btn
        binding.addCommentBtn.setOnClickListener{
            if(firebaseAuth.currentUser == null){
                // user not logged in
                Toast.makeText(this, "To comment in the book you must to log in first.", Toast.LENGTH_SHORT).show()
            }
            else {
                // user logged in
                addCommentDialog()
            }
        }
    }

    private fun loadBookDetails(){
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                   // val timestamp = "${snapshot.child("timestamps").value}"
                     bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    // format date
                    val timestamp: String = "${snapshot.child("timestamps").value}"
                    val date = if (timestamp != "null") {
                        Operations.formatTimestamptToDate(timestamp.toLong())
                    } else {
                        // Handle the invalid timestamp appropriately (e.g., set a default value or show an error)
                        "Ivalid Date"
                        // Handle the invalid timestampt
                    }
                    //load pdf category
                    Operations.loadCategory(categoryId, binding.categoryTv)

                    //load pdf size
                    Operations.getPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //thumbnail, page count
                    Operations.loadPdfFromUrlSinglePage("$bookUrl","$bookTitle", binding.pdfView, binding.progressBar, binding.pagesTv)

                    Operations.getPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //set data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Checking if book is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if(isInMyFavorite){
                        // available in favorite
                        Log.d(TAG, "onDataChange: available in favorite")
                        // set drawable top icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_filled_white, 0, 0)
                        binding.favoriteBtn.text = "Remove Favorite"
                    }
                    else {
                        //not available in favorite
                        Log.d(TAG, "onDataChange: Not available in favorite")
                        //set drawable top icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0)
                        binding.favoriteBtn.text = "Add to Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) { }
            })
    }

    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: Adding to fav...")

        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid

        if (uid == null) {
            Toast.makeText(this, "You're not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        // Merrni referencën e përdoruesit për të marrë informacionin e përdoruesit
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").value.toString()
                val userImage = snapshot.child("profileImage").value.toString()

                // Setup the HashMap for the data to be saved
                val hashMap = HashMap<String, Any>()
                hashMap["timestamp"] = timestamp
                hashMap["uid"] = uid
                hashMap["name"] = userName
                hashMap["profileImage"] = userImage

                // Referenca për bazën e të dhënave për librin dhe fushën Favorites
                val bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId).child("Favorites")

                // Save the hashmap under the Favorites field of the book
                bookRef.child(uid).setValue(hashMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "addToFavorite: Added to favorites")
                        Toast.makeText(this@PdfDetailActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "addToFavorite: Failed to add to fav due to ${e.message}")
                        Toast.makeText(this@PdfDetailActivity, "Failed to add to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showFavoriteUser(){
        //init arrayList
        usersFavoriteBookArrayList = ArrayList()

        //db path to load users who favorited the book
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Favorites")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear list
                    usersFavoriteBookArrayList.clear()
                    for(ds in snapshot.children){
                        // get data model
                        val model = ds.getValue(ModelUser::class.java)
                        // add to list
                        if (model != null) {
                            usersFavoriteBookArrayList.add(model)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle error appropriately
                }
            })
    }

    private fun showUsersDialog(users: ArrayList<ModelUser>) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_users, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Users who favorited this book")
            .create()

        val recyclerViewUsers = dialogView.findViewById<RecyclerView>(R.id.recyclerViewUsers)

        adapterUser = AdapterUsersFavorite(this, users)
        recyclerViewUsers.adapter = adapterUser


        dialog.show()
    }

    private fun fetchUsersAndShowDialog() {
        val usersList = ArrayList<ModelUser>()
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId).child("Favorites")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (ds in snapshot.children) {
                    val user = ds.getValue(ModelUser::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }
                showUsersDialog(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PdfDetailActivity, "Failed to fetch users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private var comment = ""
    private fun addCommentDialog(){
        // inflate/bind view for dialog_comment_add.xml
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        //setup alert dialog
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        // handle click - dismiss dialog
        commentAddBinding.backBtn.setOnClickListener{
            alertDialog.dismiss()
        }

        // handle click - add comment
        commentAddBinding.submitBtn.setOnClickListener{
            //get data
            comment = commentAddBinding.commentEt.text.toString().trim()

            //validate data
            if(comment.isEmpty()){
                Toast.makeText(this, "Enter comment...", Toast.LENGTH_SHORT).show()
            }
            else {
                alertDialog.dismiss()
                addComment()
            }

        }
    }

    private fun addComment(){
        //show progress
        progressDialog.setMessage("Adding Comment")
        progressDialog.show()

        //timestamp for comment id, comment timestamp etc.
        val timestamp = "${System.currentTimeMillis()}"

        //setup data to add in db for comment
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //Db path to add data: Books -> bookId -> Comments -> commentId -> commentData
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Comment added...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add comment due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showComments(){
        //init arrayList
        usersFavoriteArrayList = ArrayList()

        //db path to load comments
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear list
                    usersFavoriteArrayList.clear()
                    for(ds in snapshot.children){
                        // get datas model
                        val model = ds.getValue(ModelComment::class.java)
                        // add to list
                        usersFavoriteArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterComment = AdapterComment(this@PdfDetailActivity, usersFavoriteArrayList)

                    // set adapter to recyclerview
                    binding.commentsRv.adapter = adapterComment
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
