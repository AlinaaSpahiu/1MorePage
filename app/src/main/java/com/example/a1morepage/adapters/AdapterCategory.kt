package com.example.a1morepage.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.a1morepage.filters.FilterCategory
import com.example.a1morepage.activities.PdfListAdminActivity
import com.example.a1morepage.databinding.RowCategoryBinding
import com.example.a1morepage.models.ModelCategory
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context : Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList : ArrayList<ModelCategory>
    private var filter : FilterCategory? = null

    private lateinit var binding : RowCategoryBinding

    //constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>){
        this.context = context
        this.categoryArrayList = ArrayList(categoryArrayList)
        this.filterList = categoryArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory((binding.root))
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        //get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        // set data
        holder.categoryTv.text = category

        //handle click, delete category
        holder.deletebtn.setOnClickListener{
            //confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this category?!")
                .setPositiveButton("Confirm"){a, d->
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Cancel"){a, d->
                    a.dismiss()
                }
                .show()

        }

        //handle click, start pdf list admin activity, also pas pdf id, title
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size //number of items in list
    }

    // ViewHolder class to hold/init UI view for row_category.xml
    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView){
        //init ui views
        var categoryTv : TextView = binding.categoryTv
        var deletebtn : ImageButton = binding.deleteBtn
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory){
        //get id of category to delete
        val id = model.id//Firebase db > Categories  > categoryId
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{e->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdapterCategory", "Error deleting category", e)
            }
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}