package com.example.a1morepage.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.a1morepage.Operations
import com.example.a1morepage.databinding.RowPdfUserBinding
import com.example.a1morepage.models.ModelPdf
import com.example.a1morepage.activities.PdfDetailActivity
import com.example.a1morepage.filters.FilterPdfUser

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {
    //context, get using constructor
    private var context : Context

    //arraylist to hold pdfs, get using constructor
    public var pdfArrayList: ArrayList<ModelPdf> // to access in filter class, make it public

    //arrayList to hold filtered pdfs
    public var filterList : ArrayList<ModelPdf>

    //viewBinding row_pdf_user.xml => RowPdfUserBinding
    private lateinit var binding: RowPdfUserBinding

    private var filter: FilterPdfUser? = null

    // CONSTRUCTOR
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
      // inflate/bind layout row_pdf_user.xml
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // number of records
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        // get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //convert time
        val date = if (timestamp != null && timestamp.toString() != "null") {
            Operations.formatTimestamptToDate(timestamp)
        } else {
            "Default Date" // or whatever fallback value you want
        }


        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        Operations.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        Operations.loadCategory(categoryId, holder.categoryTv)

        Operations.getPdfSize(url, title, holder.sizeTv)

        // handle click, open pdf details page
        holder.itemView.setOnClickListener{
            // pass book id in intent, that will be user to get pdf info
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }

    // Viewholder class row_pdf_user.xml
    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView){
        //init UI components of row_pdf_user.xml
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv

    }
}