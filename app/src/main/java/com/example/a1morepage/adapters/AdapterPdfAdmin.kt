package com.example.a1morepage.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.a1morepage.models.ModelPdf
import com.example.a1morepage.Operations
import com.example.a1morepage.filters.FilterPdfAdmin
import com.example.a1morepage.activities.PdfEditActivity
import com.example.a1morepage.databinding.RowPdfAdminBinding
import com.example.a1morepage.activities.PdfDetailActivity

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    private var context: Context
    public var pdfArrayList: ArrayList<ModelPdf>
    private val filterList:ArrayList<ModelPdf>

    private lateinit var binding: RowPdfAdminBinding

    // filter object
    private var filter : FilterPdfAdmin? = null


    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super(){
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //bind/inflate layout row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //convert timestampt to: dd/MM/yyyy format
        val formatedDate = Operations.formatTimestamptToDate(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formatedDate

        //load futher details like category,pdf from url, pdf size
        //load category
        Operations.loadCategory(categoryId, holder.categoryTv)

        //load thumbnail
        Operations.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        //load pdf size
        Operations.getPdfSize(pdfUrl, title, holder.sizeTv)

        // handle click, show dialog with options1)Edit Book, 2)Delete Book
        holder.moreBtn.setOnClickListener{
            moreOptionsDialog(model, holder)
        }

        //handle item click, open pdfDetailActivity
        holder.itemView.setOnClickListener{
            // intent with book id
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId) //will be used to load book details
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size ///items count
    }


    override fun getFilter(): Filter{
        if(filter == null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return  filter as FilterPdfAdmin
    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin){
        // get id, url, title of book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //options to show in dialog
        var oprtions = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(oprtions){dialog, position ->
                // handle item click
                if(position==0){
                    //Edit
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId) //passed bookId, will be used to edit the book
                    context.startActivity(intent)
                }
                else if(position == 1){
                    //Delete
                    //show confirm dialog first

                    Operations.deleteBook(context, bookId, bookUrl, bookTitle)
                }

            }
            .show()
    }

    // View Holder class for row_pdf_admin.xml
    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){
        // UI VIews of row_pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }


}