package com.example.geschenkapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
    fun bind(profileList: ArrayList<String>) {
        if(!profileList.isEmpty()) {
            val tvGiftName: TextView = itemView.findViewById(R.id.tvGiftName)
            val tvGiftPrice: TextView = itemView.findViewById(R.id.tvGiftPrice)
            val tvGiftOwner: TextView = itemView.findViewById(R.id.tvGiftOwner)
            val tvVotes: TextView = itemView.findViewById(R.id.tvVotes)

            tvGiftName.text = profileList[1]
            tvGiftPrice.text = profileList[2]
            tvGiftOwner.text = if (profileList[9] != null) {
                profileList[8] + " " + profileList[9]
            } else {
                profileList[8]
            }
            tvVotes.text = profileList[10]
        }
    }
}