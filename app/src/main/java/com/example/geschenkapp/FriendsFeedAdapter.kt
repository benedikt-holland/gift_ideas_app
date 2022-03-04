package com.example.geschenkapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

//class CustomAdapter(private var mList: ArrayList<ItemsViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(), Filterable {
class FriendsFeedAdapter(private var giftList: ArrayList<ArrayList<String>>) : RecyclerView.Adapter<FriendsFeedAdapter.ViewHolder>(), Filterable {

    //test array
    var giftFilterList = ArrayList<ArrayList<String>>()
    init {
        for (row in giftList) {
            giftFilterList.add(row)
        }
    }
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //val ItemsViewModel = mList[position]
        //holder.bind(mList[position])
        holder.bind(giftFilterList[position])

        // sets the image to the imageview from our itemHolder class
        //holder.imageView.setImageResource(ItemsViewModel.image)

        // sets the text to the textview from our itemHolder class
        //holder.textView.text = ItemsViewModel.text

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        //return mList.size
        return giftFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<ArrayList<String>>()
                if (charSearch.isEmpty()) {
                    for (row in giftList) {
                        resultList.add(row)
                    }
                } else {
                    for (row in giftList) {
                        if (row[1].lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                }
                giftFilterList = resultList
                val filterResults = FilterResults()
                filterResults.values = giftFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                giftFilterList = results?.values as ArrayList<ArrayList<String>>
                notifyDataSetChanged()
            }

        }
    }
    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(giftList: ArrayList<String>) {
            val name: TextView = itemView.findViewById(R.id.tvName)
            val dateofbirth: TextView = itemView.findViewById(R.id.tvDateOfBirth)
            val count: TextView = itemView.findViewById(R.id.tvCountGifts)
            if (giftList[2]!=null) {
                name.text = giftList[1] + " " + giftList[2]
            } else {
                name.text = giftList[1]
            }
            dateofbirth.text = giftList[3]
            count.text = giftList[5] + " Vorschläge"

            val btnStar = itemView.findViewById(R.id.btnAddFavourite) as ImageButton
            btnStar.setOnClickListener {
                if (giftList[4].toInt()==0) {
                    //btnStar.tint = Color.YELLOW
                    giftList[4] = "1"
                } else {
                    //btnStar.tint = Color.BLACK
                    giftList[4] = "0"
                }
            }

            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {
            }
        }
    }


}
