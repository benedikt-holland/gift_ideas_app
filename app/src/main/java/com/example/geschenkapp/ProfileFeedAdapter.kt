package com.example.geschenkapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.sql.ResultSet

class ProfileFeedAdapter(private var profileFeed: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()): RecyclerView.Adapter<ProfileFeedViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfileFeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.giftfeed_card, parent, false)
        return ProfileFeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileFeedViewHolder, position: Int) {
        holder.bind(profileFeed[position])
    }

    override fun getItemCount(): Int {
        return profileFeed.size
    }

    fun updateData(newFeed: ArrayList<ArrayList<String>>) {
        profileFeed = newFeed
        notifyDataSetChanged()
    }
}

class ProfileFeedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var db: DbConnector = DbHolder.getInstance().db
    var user: ResultSet = DataHolder.getInstance().user
    fun bind(profileList: ArrayList<String>) {

        val tvVotes: TextView = itemView.findViewById(R.id.tvVotes)
        if (!profileList.isEmpty()) {
            val tvGiftName: TextView = itemView.findViewById(R.id.tvGiftName)
            val tvGiftPrice: TextView = itemView.findViewById(R.id.tvGiftPrice)
            val tvGiftOwner: TextView = itemView.findViewById(R.id.tvGiftOwner)
            val tvGiftMemberCount: TextView = itemView.findViewById(R.id.tvGiftMemberCount)

            tvGiftName.text = profileList[1]
            tvGiftPrice.text = profileList[2] + "€"
            tvGiftOwner.text = if (profileList[9] != null) {
                profileList[8] + " " + profileList[9]
            } else {
                profileList[8]
            }
            tvGiftMemberCount.text = profileList[10] + " Teilnehmer"
            tvVotes.text = profileList[11]
        }
        val btnDownvote = itemView.findViewById(R.id.btnDownvote) as ImageButton
        val btnUpvote = itemView.findViewById(R.id.btnUpvote) as ImageButton
        updateVoteColor(btnDownvote, btnUpvote, profileList[12])
        btnUpvote.setOnClickListener {
            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {
                val vote = if (profileList[12].toInt()==1) {
                    0
                } else {
                    1
                }
                var likes = db.likeGift(user.getInt("id"), profileList[0].toInt(), vote)
                withContext(Dispatchers.Main) {
                    try {
                        likes.next()
                        profileList[11] = likes.getInt(1).toString()
                        tvVotes.text = profileList[11]
                        profileList[12] = vote.toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    updateVoteColor(btnDownvote, btnUpvote, profileList[12])
                }
            }
        }

        btnDownvote.setOnClickListener {
            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {
                val vote = if (profileList[12].toInt()==-1) {
                    0
                } else {
                    -1
                }
                var likes = db.likeGift(user.getInt("id"), profileList[0].toInt(), vote)
                withContext(Dispatchers.Main) {
                    try {
                        likes.next()
                        profileList[11] = likes.getInt(1).toString()
                        tvVotes.text = profileList[11]
                        profileList[12] = vote.toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    updateVoteColor(btnDownvote, btnUpvote, profileList[12])
                }
            }
        }
    }
    fun updateVoteColor(btnDownvote: ImageButton, btnUpvote: ImageButton, like: String?) {

        if (like!=null) when(like.toInt()) {
            -1 -> {
                btnDownvote.setColorFilter(Color.argb(255, 255, 0, 0))
                btnUpvote.setColorFilter(Color.argb(255, 0, 0, 0))
            }
            0 -> {
                btnDownvote.setColorFilter(Color.argb(255, 0, 0, 0))
                btnUpvote.setColorFilter(Color.argb(255, 0, 0, 0))
            }
            1 -> {
                btnDownvote.setColorFilter(Color.argb(255, 0, 0, 0))
                btnUpvote.setColorFilter(Color.argb(255, 0, 0, 255))
            }
        }
    }
}