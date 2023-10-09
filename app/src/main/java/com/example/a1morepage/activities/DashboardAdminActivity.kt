package com.example.a1morepage.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.a1morepage.adapters.AdapterCategory
import com.example.a1morepage.models.ModelCategory
import com.example.a1morepage.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist
    private lateinit var categoryArrayList : ArrayList<ModelCategory>

    // adapter
    private lateinit var adapterCategory : AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        // search
        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Invoke your filter here
               try{
                   adapterCategory.filter.filter(s)
                  }catch(e:Exception) { }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        // Button - Logout BTN
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

        // Button - Add new PDF Book
        binding.addPdfFab.setOnClickListener{
            startActivity(Intent(this, PdfAddActivity::class.java))
        }

        // Button - Add new Category btn
        binding.addCategoryBtn.setOnClickListener{
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        // Button - Open Profile btn
        binding.profileBtn.setOnClickListener{
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

    private fun checkUser(){
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //logged in,
            val email = firebaseUser.email
            // show email in toolbar
            binding.subTitleTv.text = email
        }
    }

    private fun loadCategories(){
        // init arrayList
        categoryArrayList = ArrayList()

        //get all categories from firebase database... Firebase DB > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    //get data as model
                    var model = ds.getValue(ModelCategory::class.java)

                    //add to arrayList
                    categoryArrayList.add(model!!)
                }
                // setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
               // set adapter to recyclerview
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardAdminActivity", "Error loading categories", error.toException())
            }
        })
    }
}