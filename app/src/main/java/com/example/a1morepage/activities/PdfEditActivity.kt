package com.example.a1morepage.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.a1morepage.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPdfEditBinding

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }

    private var bookId = ""//book id from intent started from AdapterPdfAdmin

    private lateinit var progresDialog : ProgressDialog

    //arraylist to hold category titles
    private lateinit var categoryTitleArrayList : ArrayList<String>

    //arraylist to hold category ids
    private lateinit var categoryIdArrayList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id to edit the book info
        bookId = intent.getStringExtra("bookId")!!

        //setup progresDialog
        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Please wait")
        progresDialog.setCanceledOnTouchOutside(false)

        // loadCategories
        loadCategories()
        loadBookInfo()

        //handle click - goBack
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //handle click - pick category
        binding.categoryTv.setOnClickListener{
            categoryDialog()
        }

        //handle click - Update button
        binding.submitBtn.setOnClickListener{
            validateData()
            onBackPressed()

        }

    }

    private fun loadCategories(){
        Log.d(TAG, "loadCategories: loading categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into them
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for(ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category Title $id")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private var selectedCategoryId  = ""
    private var selectedCategoryTitle = ""
    private fun categoryDialog(){
        // Show dialog to pick the category of pdf/book

        //make string array from arraylist of string
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)

        for(i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }
        //alert dialog
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray){dialog, position ->
                //handle click, save clicked category id and title
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                //set to textview
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show() //shows dialog




    }

    private var title =""
    private var description = ""
    private fun validateData(){
        // get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        //validate data
        if(title.isEmpty()){
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        } else if(description.isEmpty()){
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show()
        } else if(selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Select Category", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf(){
        Log.d(TAG, "updatePdf: Starting updating pdf info...")

        //show progress
        progresDialog.setMessage("Updating book info")
        progresDialog.show()

        //setup data to update to db
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        //start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progresDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated successfully...")
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                Log.d(TAG, "updatePdf: Failes to update due to ${e.message}")
                progresDialog.dismiss()
                Toast.makeText(this, "Failed to update due to {${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun loadBookInfo(){
        Log.d(TAG, "loadBookInfo: Loading book info")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    // set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //load book category info using categoryId
                    Log.d(TAG, "onDataChange: Loafind book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value

                                //set to textview
                                binding.categoryTv.text = category.toString()

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}