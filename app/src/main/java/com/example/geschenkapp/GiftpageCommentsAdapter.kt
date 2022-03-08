package com.example.geschenkapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class GiftpageCommentsAdapter(private var friendsList: ArrayList<ArrayList<String>>) : RecyclerView.Adapter<GiftpageCommentsAdapter.FriendsFeedViewHolder>() {
    //test array
    var friendsFilterList = ArrayList<ArrayList<String>>()
    init {
        for (row in friendsList) {
            friendsFilterList.add(row)
        }
    }
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsFeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friendsfeed_card, parent, false)

        return FriendsFeedViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holderFriendsFeed: FriendsFeedViewHolder, position: Int) {
        holderFriendsFeed.bind(friendsFilterList[position])

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return friendsFilterList.size
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

