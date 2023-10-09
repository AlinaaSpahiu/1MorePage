package com.example.a1morepage.filters

import android.widget.Filter
import com.example.a1morepage.adapters.AdapterPdfUser
import com.example.a1morepage.models.ModelPdf

class FilterPdfUser : Filter {
    // arraylist in which we want to search
    var filterList : ArrayList<ModelPdf>

    // ADAPTER IN WHICH FILTER NEED TO BE IMPLEMENTED
    var adapterPdfUser : AdapterPdfUser

    // constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
     }


    //
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint : CharSequence? = constraint

        val results = FilterResults()

        //value to be searched should not be null nor empty
        if(constraint != null && constraint.isNotEmpty()){
            // not null nor empty

            //change to upper case to avoid case sensitivity
            constraint = constraint.toString().uppercase()

            val filteredModels = ArrayList<ModelPdf>()
            for(i in filterList.indices){
                //validate if match
                if(filterList[i].title.uppercase().contains(constraint)){
                    //searched value matched with title, add to list
                    filteredModels.add(filterList[i])
                }
            }

            // return filtered list and size
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else {
            // either it is null or empty
            //return original list and size
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        // apply filter changes
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        // notify changes
        adapterPdfUser.notifyDataSetChanged()
    }

}