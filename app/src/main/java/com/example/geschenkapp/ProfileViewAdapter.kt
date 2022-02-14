package com.example.geschenkapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.data.Gift

class ProfileViewAdapter : RecyclerView.Adapter<ProfileViewAdapter.ViewHolder>() {

    var giftList : ArrayList<Gift> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.giftfeed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = giftList[position].name
    }

    override fun getItemCount(): Int {
        return giftList.size
    }

    fun setGiftfeed(giftList:ArrayList<Gift>) {
        this.giftList = giftList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName : TextView = itemView.findViewById(R.id.textView)
    }
}