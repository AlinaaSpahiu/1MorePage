package com.example.a1morepage.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.a1morepage.Operations
import com.example.a1morepage.activities.PdfDetailActivity
import com.example.a1morepage.databinding.RowPdfFavoriteBinding
import com.example.a1morepage.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>{
    //Context
    private val context : Context

    // ArrayList to hold books
    private var booksArrayList: ArrayList<ModelPdf>

    //view binding
    private lateinit var binding : RowPdfFavoriteBinding

    constructor(context: Context, booksArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        // bind/inflate row_pdf_favorite.xml
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfFavorite(binding.root)
    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        // Get data form db "users"-> "uid" -> "Favorites
        val model = booksArrayList[position]

        loadBookDetails(model, holder)

        // handle click, open pdf details, pass book id to load details
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }

        //handle click, remove from favorite
        holder.removeFavBtn.setOnClickListener{
            Operations.removeFromFavorite(context, model.id)
        }

    }

    public fun loadBookDetails(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite){
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book info
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp"). value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    // set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId

                    if (timestamp != "null") {
                        try {
                            model.timestamp = timestamp.toLong()
                        } catch (e: NumberFormatException) {
                            model.timestamp = 0L
                        }
                    } else {
                        model.timestamp = 0L
                    }

                    model.uid = uid
                    model.url = url

                    if (viewsCount != "null") {
                        try {
                            model.viewsCount = viewsCount.toLong()
                        } catch (e: NumberFormatException) {
                            model.viewsCount = 0L
                        }
                    } else {
                        model.viewsCount = 0L
                    }

                    // format date
                    val date = Operations.formatTimestamptToDate(model.timestamp!!)

                    Operations.loadCategory("$categoryId", holder.categoryTv)
                    Operations.loadPdfFromUrlSinglePage("$url","$title", holder.pdfView, holder.progressBar, null)
                    Operations.getPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {   }
            })
    }


    // View holder class to manage UI views of row_pdf_favorite.xml
    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView){
        //init UI Views
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavBtn = binding.removeFavBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }
}