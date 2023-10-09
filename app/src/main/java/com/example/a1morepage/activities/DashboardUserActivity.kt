package com.example.a1morepage.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.a1morepage.adapters.AdapterCategory
import com.example.a1morepage.adapters.AdapterPdfUser
import com.example.a1morepage.models.ModelCategory
import com.example.a1morepage.databinding.ActivityDashboardUserBinding
import com.example.a1morepage.users.BooksUserFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    // variables
    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList : ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter : ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)


        //Logout - button
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //Button to open profile
        binding.profileBtn.setOnClickListener{
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    //Kjo metodë konfiguron ViewPager duke krijuar një adapter të ri
    // dhe duke e mbushur atë me fragmente të kategorive të ndryshme të librave nga databaza Firebase.
    private fun setupWithViewPagerAdapter(viewpager : ViewPager){
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        //init list
        categoryArrayList = ArrayList()

        //get categories from database
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()

                //load some static categories like All, Mst Viewed, Most downloaded
                //all data to models
                val modelAll = ModelCategory("01", "All", "1", "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", "1", "")


                // add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)


                //add to viewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )


                //refresh list
                viewPagerAdapter.notifyDataSetChanged()

                //load from firebase db
                for(ds in snapshot.children){
                    // get data in model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to list
                    categoryArrayList.add(model!!)

                    //add to viewPagerAdapter
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )

                    // refresh list
                    viewPagerAdapter.notifyDataSetChanged()

                }

            }
            override fun onCancelled(error: DatabaseError) {    }
        })

        //setup adapter to viewpager
        viewpager.adapter = viewPagerAdapter
    }


    // Kjo është një klasë ndihmëse për të menaxhuar fragmentet në ViewPager.
    // Ajo mban një listë të fragmenteve dhe titujve të tyre për të shfaqur në TabLayout.
    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) : FragmentPagerAdapter(fm, behavior ){
        //holds list of fragments psh new instances of same fragment for each category
        private val fragmentsList: ArrayList<BooksUserFragment> = ArrayList()

        // list of titles of categories for tabs
        private val fragmentTitleList : ArrayList<String> = ArrayList()

        private val context : Context

        init {
            this.context = context
        }

        override fun getCount() : Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int) : Fragment {
        return fragmentsList[position]
        }

        override  fun getPageTitle(position: Int) : CharSequence? {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String){
            // add fragment that will be passed as parameter in fragmentlst
            fragmentsList.add(fragment)

            // add title that will be passed as parameter
            fragmentTitleList.add(title)
        }

    }

    // CheckUser method
    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {

            binding.subTitleTv.text = "Not Logged in"

            //hide profile, logout
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        } else {
            //logged in, get and show user info
            val email = firebaseUser.email
            //set to textview of toolbas
            binding.subTitleTv.text = email

            // show profile, logout
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }
}