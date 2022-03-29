package com.example.geschenkapp

import android.annotation.SuppressLint
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

//Adapter for gift cards on profile page
class ProfileFeedAdapter(private var profileFeed: ArrayList<ArrayList<String>> = ArrayList()): RecyclerView.Adapter<ProfileFeedViewHolder>() {
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFeed: ArrayList<ArrayList<String>>) {
        profileFeed = newFeed
        notifyDataSetChanged()
    }
}

//View holder for gift cards on profile page
class ProfileFeedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var db: DbConnector = DbHolder.getInstance().db
    var user: ResultSet = LoginHolder.getInstance().user
    @SuppressLint("SetTextI18n")
    fun bind(profileList: ArrayList<String>) {
        //Set text view content
        val tvVotes: TextView = itemView.findViewById(R.id.tvVotes)
        if (profileList.isNotEmpty()) {
            val tvGiftName: TextView = itemView.findViewById(R.id.tvGiftName)
            val tvGiftPrice: TextView = itemView.findViewById(R.id.tvGiftPrice)
            val tvGiftOwner: TextView = itemView.findViewById(R.id.tvGiftOwner)
            val tvGiftMemberCount: TextView = itemView.findViewById(R.id.tvGiftMemberCount)

            tvGiftName.text = profileList[1]
            tvGiftPrice.text = profileList[2] + itemView.context.getString(R.string.currency)
            tvGiftOwner.text = profileList[8] + " " + profileList[9]
            tvGiftMemberCount.text = profileList[10] + " " + itemView.context.getString(R.string.members)
            tvVotes.text = profileList[11]
        }
        //Register vote buttons
        val btnDownvote = itemView.findViewById(R.id.btnDownvote) as ImageButton
        val btnUpvote = itemView.findViewById(R.id.btnUpvote) as ImageButton
        updateVoteColor(btnDownvote, btnUpvote, profileList[13])
        btnUpvote.setOnClickListener {
            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {
                val vote = if (profileList[13]=="1") {
                    0
                } else {
                    1
                }
                //Push like to database
                val likes = db.likeGift(user.getInt("id"), profileList[0].toInt(), vote)
                withContext(Dispatchers.Main) {
                    try {
                        //Update local data
                        likes.next()
                        profileList[11] = likes.getInt(1).toString()
                        tvVotes.text = profileList[11]
                        profileList[13] = vote.toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    //Update button color
                    updateVoteColor(btnDownvote, btnUpvote, profileList[13])
                }
            }
        }

        btnDownvote.setOnClickListener {
            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {
                val vote = if (profileList[13]=="-1") {
                    0
                } else {
                    -1
                }
                val likes = db.likeGift(user.getInt("id"), profileList[0].toInt(), vote)
                withContext(Dispatchers.Main) {
                    try {
                        likes.next()
                        profileList[11] = likes.getInt(1).toString()
                        tvVotes.text = profileList[11]
                        profileList[13] = vote.toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    updateVoteColor(btnDownvote, btnUpvote, profileList[13])
                }
            }
        }

        //Listener for clicking on gift cards, opens detail gift page
        val btnCard = itemView.findViewById(R.id.cvGift) as CardView
        btnCard.setOnClickListener {
            val intent = Intent(itemView.context, GiftpageActivity::class.java)
            val b = Bundle()
            b.putInt("id", profileList[0].toInt())
            b.putInt("profileUserId", profileList[14].toInt())
            intent.putExtras(b)
            itemView.context.startActivity(intent)
        }


    }

    //Update color of vote buttons
    //Downvote: Red, Upvote: Blue
    private fun updateVoteColor(btnDownvote: ImageButton, btnUpvote: ImageButton, like: String?) {

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