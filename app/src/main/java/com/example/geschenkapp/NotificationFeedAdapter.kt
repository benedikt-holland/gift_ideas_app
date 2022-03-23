package com.example.geschenkapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.sql.ResultSet

//Adapter for gift cards on profile page
class NotificationFeedAdapter(private var notificationFeed: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()): RecyclerView.Adapter<NotificationFeedViewHolder>() {
    /* notificationFeed
        0 -> Notification Type: Int
        1 -> User name: String
        2 -> Gift name: String
        3 -> User Id: Int
        4 -> Gift Id: Int
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
        holder.bind(notificationFeed[position])
    }

    override fun getItemCount(): Int {
        return notificationFeed.size
    }

    fun updateData(newFeed: ArrayList<ArrayList<String>>) {
        notificationFeed = newFeed
        notifyDataSetChanged()
    }
}

//View holder for gift cards on profile page
class NotificationFeedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var db: DbConnector = DbHolder.getInstance().db
    var user: ResultSet = DataHolder.getInstance().user
    fun bind(notificationsList: ArrayList<String>) {
        //Set text view content
        if (notificationsList.isNotEmpty()) {
            //Add last name if exists
            val name = notificationsList[2] + if(notificationsList[3]!=null) {
                " " + notificationsList[3]
            } else {
                ""
            }
            val tvNotification: TextView = itemView.findViewById(R.id.tvNotification)
            val tvNotificationText: TextView = itemView.findViewById(R.id.tvNotificationText)

            tvNotification.text = when(notificationsList[1].toInt()) {
                0 -> itemView.context.getString(R.string.friends_request)
                1 -> itemView.context.getString(R.string.gift_join_request)
                2 -> itemView.context.getString(R.string.gift_change)
                3 -> itemView.context.getString(R.string.gift_deleted)
                else -> tvNotification.text
            }
            tvNotificationText.text = when(notificationsList[1].toInt()) {
                0 -> name + " " + itemView.context.getString(R.string.friends_request_text)
                1 -> name + " " + itemView.context.getString(R.string.text_would_like_to) + " " + notificationsList[3] + " " + itemView.context.getString(R.string.gift_join_text)
                2 -> notificationsList[3] + " " + itemView.context.getString(R.string.text_for) + " " + name + " " + itemView.context.getString(R.string.gift_change_text)
                3 -> notificationsList[3] + " " + itemView.context.getString(R.string.text_for) + " " + name + " " + itemView.context.getString(R.string.gift_deleted_text)
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
                    db.removeNotification(notificationsList[0].toInt())
                }
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
                        0 -> db.addFriend(user.getInt("id"), notificationsList[4].toInt())
                        1 -> db.joinGift(notificationsList[4].toInt(), notificationsList[5].toInt())
                    }
                    db.removeNotification(notificationsList[0].toInt())
                }
            }

            btnDecline.setOnClickListener {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {
                    db.removeNotification(notificationsList[0].toInt())
                }
            }
        }
    }
}
