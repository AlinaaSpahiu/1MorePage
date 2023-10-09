package com.example.a1morepage.filters

import android.widget.Filter
import com.example.a1morepage.adapters.AdapterPdfAdmin
import com.example.a1morepage.models.ModelPdf


class FilterPdfAdmin : Filter {
    //arrayList from where we search
    var filterList: ArrayList<ModelPdf>

    //adapter in which filter need to be implemented
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint //value to search
        val results = FilterResults()

        //value to be searched should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().lowercase() //using this to avoid case sensitivity

            var filteredModels = ArrayList<ModelPdf>()
            for(i in filterList.indices){
                if(filterList[i].title.lowercase().contains(constraint)){
                    //searched value is similar to value in list, add to filtered list
                     filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            // searched value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }


    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }

}
