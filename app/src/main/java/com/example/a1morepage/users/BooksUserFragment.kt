package com.example.a1morepage.users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.a1morepage.adapters.AdapterPdfUser

import com.example.a1morepage.databinding.FragmentBooksUserBinding
import com.example.a1morepage.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment : Fragment {

    //view binding fragment_books_user.xml => FragmentBooksUserBinding
    private lateinit var binding : FragmentBooksUserBinding

    public companion object{
        private const val TAG = "BOOKS_USER_TAG"

        //recive data from activity to load books e.g. categoryId, category, uid
        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment {
            val fragment = BooksUserFragment()

            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList : ArrayList<ModelPdf>
    private lateinit var adapterPdfUser : AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        //get arguments that we passed in newInstance method

        val args = arguments
        if(args != null){
            categoryId = args?.getString("categoryId") ?: ""
            category = args?.getString("category")?: ""
            uid = args?.getString("uid")?: ""
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context),container, false)

        // load pdf according to category, this fragment will have new instance to load each category pdfs
        Log.d(TAG, "onCreateView: Category: $category")
        if(category == "All"){
            // load all books
            loadAllBooks()
        } else if (category == "Most Viewed"){
            // load mos viewed books
            loadMostViewedDownloadedBooks("viewsCount")
        } else if(category == "Most Downloaded"){
            // load most downloaded books
            loadMostViewedDownloadedBooks("downloadsCount")

        } else {
            // load selected category books
            loadCategorizedBookd()
        }

        // search
        binding.searchEt.addTextChangedListener{object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try {
                        adapterPdfUser.filter.filter(s)
                    }catch (e:Exception){
                        Log.d(TAG, "onTextChanged: SEARCH EXCEPTION: ${e.message}")
                    }
            }

            override fun afterTextChanged(s: Editable?) { }

        }

        }
        return binding.root
    }

    private fun loadAllBooks(){
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            //clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    // add to list
                    pdfArrayList.add(model!!)
                }
                // setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                //set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun loadMostViewedDownloadedBooks(orderBy: String){
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10) // load 10 mos viewed or downloaded
            .addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    // add to list
                    if (model != null) {
                        pdfArrayList.add(model)
                    }else {
                        Log.e(TAG, "Error parsing model for data snapshot: ${ds.key}")
                    }
                }
                // setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                //set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun loadCategorizedBookd(){
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before starting adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        // add to list
                        pdfArrayList.add(model!!)
                    }
                    // setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    //set adapter to recyclerview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}