package com.example.geschenkapp

import android.annotation.SuppressLint
import android.content.Intent
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
class NotificationFeedAdapter(private var notificationFeed: ArrayList<ArrayList<String>> = ArrayList()): RecyclerView.Adapter<NotificationFeedViewHolder>() {
    /* notificationFeed
        0 -> Notification Id: Int
            0: Friends request, 1: gift join request, 2: Gift change notification, 3: Gift delete notification, 4: Gift leave
        1 -> Notification Type: Int
        2 -> User first name: String
        3 -> User last name: String
        4 -> Gift name: String
        5 -> User Id: Int
        6 -> Gift Id: Int
        7 -> Profile user first name: String
        8 -> Profile user last name: String
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationFeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_card, parent, false)
        return NotificationFeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationFeedViewHolder, position: Int) {
        holder.bind(notificationFeed[position], this)
    }

    override fun getItemCount(): Int {
        return notificationFeed.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFeed: ArrayList<ArrayList<String>>) {
        notificationFeed = newFeed
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        notificationFeed.removeAt(position)
        notifyItemRemoved(position)
    }
}

//View holder for gift cards on profile page
class NotificationFeedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var db: DbConnector = DbHolder.getInstance().db
    var user: ResultSet = LoginHolder.getInstance().user
    fun bind(notificationsList: ArrayList<String>, adapter: NotificationFeedAdapter) {
        //Set text view content
        if (notificationsList.isNotEmpty()) {
            //Add last name if exists
            val name = notificationsList[2] + (" " + notificationsList[3])
            val giftUsername: String = notificationsList[7] + " " + notificationsList[8]
            val tvNotification: TextView = itemView.findViewById(R.id.tvNotification)
            val tvNotificationText: TextView = itemView.findViewById(R.id.tvNotificationText)

            tvNotification.text = when(notificationsList[1].toInt()) {
                0 -> itemView.context.getString(R.string.friends_request)
                1 -> itemView.context.getString(R.string.gift_join_request)
                2 -> itemView.context.getString(R.string.gift_change)
                3 -> itemView.context.getString(R.string.gift_deleted)
                4 -> itemView.context.getString(R.string.gift_left)
                else -> tvNotification.text
            }
            tvNotificationText.text = when(notificationsList[1].toInt()) {
                0 -> name + " " + itemView.context.getString(R.string.friends_request_text)
                1 -> name + " " +
                        itemView.context.getString(R.string.text_would_like_to) + " " +
                        notificationsList[4] + " " +
                        itemView.context.getString(R.string.text_for) + " " +
                        giftUsername + " " +
                        itemView.context.getString(R.string.gift_join_text)
                2 -> notificationsList[4] + " " +
                        itemView.context.getString(R.string.text_for) + " " +
                        giftUsername + " " +
                        itemView.context.getString(R.string.gift_change_text)
                3 -> notificationsList[4] + " " +
                        itemView.context.getString(R.string.text_for) + " " +
                        giftUsername + " " +
                        itemView.context.getString(R.string.gift_deleted_text)
                4 -> name + " " +
                        itemView.context.getString(R.string.text_left) + " " +
                        notificationsList[4] + " " +
                        itemView.context.getString(R.string.text_for) + " " +
                        giftUsername + " " +
                        itemView.context.getString(R.string.text_left2)
                else -> tvNotificationText.text
            }
        }
        //Register buttons
        val btnAccept: ImageButton = itemView.findViewById(R.id.btnAccept)
        val btnDecline: ImageButton = itemView.findViewById(R.id.btnDecline)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
        if (notificationsList[1].toInt() > 1) {
            /*Notifications for gift change and gift delete
            Only show remove button */
            btnAccept.visibility = View.GONE
            btnDecline.visibility = View.GONE
            btnRemove.visibility = View.VISIBLE

            btnRemove.setOnClickListener {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {
                    db.removeNotificationById(notificationsList[0].toInt())
                }
                adapter.removeItem(bindingAdapterPosition)
            }

        } else {
            /* Notifications for friends request and gift join request
            Show accept and decline buttons*/
            btnAccept.visibility = View.VISIBLE
            btnDecline.visibility = View.VISIBLE
            btnRemove.visibility = View.GONE

            btnAccept.setOnClickListener {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {
                    when (notificationsList[1].toInt()) {
                        0 -> db.addFriend(user.getInt("id"), notificationsList[5].toInt())
                        1 -> db.joinGift(notificationsList[5].toInt(), notificationsList[6].toInt())
                    }
                    db.removeNotificationById(notificationsList[0].toInt())
                }
                adapter.removeItem(bindingAdapterPosition)
            }

            btnDecline.setOnClickListener {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {
                    db.removeNotificationById(notificationsList[0].toInt())
                }
                bindingAdapter!!.notifyItemRemoved(bindingAdapterPosition)
            }
        }
        //Set click listener for notification card
        val btnCard = itemView.findViewById(R.id.cvNotification) as CardView
        btnCard.setOnClickListener {
            when(notificationsList[1].toInt()) {
                //Do nothing
                3 -> {}
                //Open gift page
                2, 4 -> {
                    val intent = Intent(itemView.context, GiftpageActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", notificationsList[6].toInt())
                    intent.putExtras(b)
                    itemView.context.startActivity(intent)
                }
                //Open profile page
                else -> {
                    val intent = Intent(itemView.context, ProfileActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", notificationsList[5].toInt())
                    intent.putExtras(b)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }
}
