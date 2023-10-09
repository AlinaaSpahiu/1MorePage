package com.example.a1morepage.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.a1morepage.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //No account?! - Sign Up
        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //handle click begin login
        binding.loginBtn.setOnClickListener{
            validateData()
        }
    }

    // my methods
    private var email = ""
    private var password = ""

    private fun validateData(){
        //get data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show()
        }else if (password.isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        }else {
            loginUser()
        }
    }

    private fun loginUser(){
        progressDialog.setMessage("Logging in...")
        progressDialog.show()

        //create use in firebase auth
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser() // add user to db
            }
            .addOnFailureListener{e->
                //log in failed
                progressDialog.dismiss()
                Toast.makeText(this, "Failed login due to ${e.message}..", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser(){
        progressDialog.setMessage("Checking user...")

        val firebaseAuth = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    //get user type user or admin
                    val userType = snapshot.child("userType").value
                    if(userType == "user"){
                        startActivity((Intent(this@LoginActivity, DashboardUserActivity::class.java)))
                        finish()
                    } else if(userType == "admin"){
                        startActivity((Intent(this@LoginActivity, DashboardAdminActivity::class.java)))
                        finish()
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}