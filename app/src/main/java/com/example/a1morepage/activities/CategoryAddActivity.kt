package com.example.a1morepage.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.a1morepage.Operations
import com.example.a1morepage.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class  CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding:ActivityCategoryAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        // click btn - Back button
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // click btn - Submit btn
        binding.submitBtn.setOnClickListener{
            validateData()
        }
    }

    private var category = ""
    private fun validateData(){
        //get data
        category = binding.categoryEt.text.toString().trim()

        //validate data
        if(category.isEmpty()){
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        }else{
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase(){
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        //setup data to add in firebase db
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = "$timestamp"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to firebase db: Database Root > Categories > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added Successfully!", Toast.LENGTH_SHORT).show()
                Operations.clearInputFields(binding.categoryEt)

                // Navigate to CategoryListActivity
                val intent = Intent(this, DashboardAdminActivity::class.java)
                startActivity(intent)
                finish()  // optionally, if you want to remove this activity from the back stack

            }
            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}