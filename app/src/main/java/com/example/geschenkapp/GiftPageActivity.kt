package com.example.geschenkapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.sql.ResultSet

class GiftPageActivity  : AppCompatActivity() {

    lateinit var user: ResultSet
    lateinit var friendsFeed: ResultSet
    lateinit var giftPageCommentsAdapter: GiftpageCommentsAdapter
    lateinit var giftPageCommentsRv: RecyclerView
    private var db = DbConnector()
    private var giftId: Int = -1
    private var memberId: Int? = null
    private var userId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giftpage)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val b: Bundle? = intent.extras
        if (b != null) {
            giftId = b.getInt("id")
        }
        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        val tvName: TextView = findViewById(R.id.tvName)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvLink: TextView = findViewById(R.id.tvLink)
        val tvOwner: TextView = findViewById(R.id.tvOwner)
        val tvPrivacy: TextView = findViewById(R.id.tvPrivacy)
        val tvMemberCount: TextView = findViewById(R.id.tvMemberCount)
        val btnJoin: Button = findViewById(R.id.btnJoin)

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            userId = user.getInt("id")
            var gift = db.getGiftById(userId, giftId)
            if (gift.next()) {
                withContext(Dispatchers.Main) {
                    tvName.text = gift.getString("title")
                    tvPrice.text = gift.getString("price") + "€"
                    tvLink.text = gift.getString("gift_link")
                    if (gift.getString("owner_last_name") != null) {
                        tvOwner.text =
                            gift.getString("owner_first_name") + " " + gift.getString("owner_last_name")
                    } else {
                        tvOwner.text = gift.getString("owner_first_name")
                    }
                    supportActionBar?.apply {
                        if (gift.getString("user_last_name") != null) {
                            title =
                                gift.getString("user_first_name") + " " + gift.getString("user_last_name")
                        } else {
                            title = gift.getString("user_first_name")
                        }
                    }
                    tvPrivacy.text = when (gift.getInt("post_privacy")) {
                        0 -> {
                            "Öffentlich"
                        }
                        1 -> {
                            "Nur Freunde"
                        }
                        2 -> {
                            "Nur Freunde"
                        }
                        else -> {
                            "Unsichtbar"
                        }
                    }
                    tvMemberCount.text = gift.getString("member_count")

                    updateJoinButtonColor(btnJoin, gift.getInt("member_id"))
                    btnJoin.setOnClickListener {
                        val viewModelJob = SupervisorJob()
                        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                        uiScope.launch(Dispatchers.IO) {
                            try {
                                memberId = gift.getInt("member_id")
                            } catch (e: Exception) {
                                memberId = null
                            }
                            if(memberId != null) {
                                db.leaveGift(memberId!!)
                                memberId = null
                            } else {
                                val memberSet: ResultSet = db.joinGift(userId, giftId)
                                if (memberSet.next()) {
                                    memberId = memberSet.getInt(1)
                                }
                            }
                            updateJoinButtonColor(btnJoin, memberId)
                        }
                    }
                }
            }
        }

        /*giftPageCommentsRv = findViewById(R.id.rvGiftPageComments)
        giftPageCommentsRv.layoutManager = LinearLayoutManager(giftPageCommentsRv.context)
        giftPageCommentsRv.setHasFixedSize(true)*/

    }

    fun updateJoinButtonColor(btnJoin: Button, memberId: Int?) {
        if(memberId!=null) {
            btnJoin.text = "Verlassen"
            btnJoin.setBackgroundColor(Color.argb(255, 255, 0, 0))
        } else {
            btnJoin.text = "Teilnehmen"
            btnJoin.setBackgroundColor(Color.argb(255, 50, 205, 50))
        }
    }

    override fun onBackPressed() {
        var intent = Intent(this, ProfileActivity::class.java)
        var b = Bundle()
        b.putInt("id", userId)
        intent.putExtras(b)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivityIfNeeded(intent, 0)
    }



    /*suspend fun loadFriendsFeed(userId: Int) {
        friendsFeed = db.getFriendsFeed(userId)
        val friendsFeedArray = unloadResultSet(friendsFeed)
        withContext(Dispatchers.Main) {
            giftPageCommentsAdapter = GiftpageCommentsAdapter(friendsFeedArray)
            giftPageCommentsRv.adapter = giftPageCommentsAdapter
            giftPageCommentsAdapter.notifyDataSetChanged()
        }
    }*/
}