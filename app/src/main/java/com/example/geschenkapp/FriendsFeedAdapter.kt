package com.example.geschenkapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

//class CustomAdapter(private var mList: ArrayList<ItemsViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(), Filterable {
class FriendsFeedAdapter(private var friendsList: ArrayList<ArrayList<String>>) : RecyclerView.Adapter<FriendsFeedAdapter.FriendsFeedViewHolder>(), Filterable {

    //test array
    var friendsFilterList = ArrayList<ArrayList<String>>()
    init {
        for (row in friendsList) {
            friendsFilterList.add(row)
        }
    }
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsFeedViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friendsfeed_card, parent, false)

        return FriendsFeedViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holderFriendsFeed: FriendsFeedViewHolder, position: Int) {

        //val ItemsViewModel = mList[position]
        //holder.bind(mList[position])
        holderFriendsFeed.bind(friendsFilterList[position])

        // sets the image to the imageview from our itemHolder class
        //holder.imageView.setImageResource(ItemsViewModel.image)

        // sets the text to the textview from our itemHolder class
        //holder.textView.text = ItemsViewModel.text

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        //return mList.size
        return friendsFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<ArrayList<String>>()
                if (charSearch.isEmpty()) {
                    for (row in friendsList) {
                        resultList.add(row)
                    }
                } else {
                    for (row in friendsList) {
                        val name = if(row[3]!=null) {
                           row[2] + " " + row[3]
                        } else {
                            row[2]
                        }
                        if (row[2].lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || (row[3]!=null && row[3].lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) ||
                            name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                }
                friendsFilterList = resultList
                val filterResults = FilterResults()
                filterResults.values = friendsFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                friendsFilterList = results?.values as ArrayList<ArrayList<String>>
                notifyDataSetChanged()
            }

        }
    }
    // Holds the views for adding it to image and text
    class FriendsFeedViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(friendsList: ArrayList<String>) {
            val name: TextView = itemView.findViewById(R.id.tvName)
            val dateofbirth: TextView = itemView.findViewById(R.id.tvFeedDateofbirth)
            val count: TextView = itemView.findViewById(R.id.tvCountGifts)
            if (friendsList[3]!=null) {
                name.text = friendsList[2] + " " + friendsList[3]
            } else {
                name.text = friendsList[2]
            }
            dateofbirth.text = friendsList[4]
            count.text = friendsList[6] + " Vorschläge"

            val btnStar = itemView.findViewById(R.id.btnAddFavourite) as ImageButton
            btnStar.setOnClickListener {
                if (friendsList[5].toInt()==0) {
                    //btnStar.tint = Color.YELLOW
                    friendsList[5] = "1"
                } else {
                    //btnStar.tint = Color.BLACK
                    friendsList[5] = "0"
                }
            }

            val btnCard = itemView.findViewById(R.id.cvFriend) as CardView
            btnCard.setOnClickListener {
                var intent = Intent(itemView.context, ProfileActivity::class.java)
                var b = Bundle()
                b.putInt("id", friendsList[1].toInt())
                intent.putExtras(b)
                itemView.context.startActivity(intent)
            }
        }
    }


}
