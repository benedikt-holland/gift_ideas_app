package com.example.geschenkapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
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
    private var profileUserId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isNew = false
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
            profileUserId = b.getInt("profileUserId")
        }
        isNew = giftId == 0
        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        val tvName: TextView = findViewById(R.id.tvName)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvLink: TextView = findViewById(R.id.tvLink)
        val tvOwner: TextView = findViewById(R.id.tvOwner)
        val spPrivacy: Spinner = findViewById(R.id.spGiftpagePostPrivacy)
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
                    tvPrice.text = gift.getString("price")
                    tvLink.text = gift.getString("gift_link")
                    if (gift.getString("owner_last_name") != null) {
                        tvOwner.text =
                            gift.getString("owner_first_name") + " " + gift.getString("owner_last_name")
                    } else {
                        tvOwner.text = gift.getString("owner_first_name")
                    }
                    if(gift.getInt("owner_id")==userId) {
                        tvName.isEnabled = true
                        tvPrice.isEnabled = true
                        tvLink.isEnabled = true
                        spPrivacy.isEnabled = true
                    } else {
                        tvName.isEnabled = false
                        tvPrice.isEnabled = false
                        tvLink.isEnabled = false
                        spPrivacy.isEnabled = false
                    }
                    supportActionBar?.apply {
                        if (gift.getString("user_last_name") != null) {
                            title =
                                gift.getString("user_first_name") + " " + gift.getString("user_last_name")
                        } else {
                            title = gift.getString("user_first_name")
                        }
                        tvPrivacy.text = when (gift.getInt("post_privacy")) {
                            0 -> {
                                getString(R.string.postPrivacyPublic)
                            }
                            1 -> {
                                getString(R.string.postPrivacyOnlyFriendsOfOwner)
                            }
                            2 -> {
                                getString(R.string.postPrivacyOnlyFriendsOfOwnerHiddenMembers)
                            }
                            else -> {
                                getString(R.string.postPrivacyHidden)
                            }
                        }
                        tvMemberCount.text = gift.getString("member_count")
                    }
                }
                    if(!isNew) {
                        updateJoinButtonColor(
                            btnJoin,
                            gift.getInt("member_id"),
                            gift.getInt("owner_id")
                        )
                    } else {
                        updateJoinButtonColor(btnJoin, null, userId)
                    }
                    btnJoin.setOnClickListener {
                        val viewModelJob = SupervisorJob()
                        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                        uiScope.launch(Dispatchers.IO) {
                            if (isNew || userId == gift.getInt("owner_id")) {
                                val profilePrivacyArray: Array<String> =
                                    resources.getStringArray(R.array.profile_privacy_array)
                                var postPrivacy: Int = 0
                                for (i in profilePrivacyArray.indices) {
                                    if (profilePrivacyArray[i].contains(tvPrivacy.text.toString())) {
                                        postPrivacy = i
                                    }
                                }
                                try {
                                        db.updateGift(
                                            if(isNew) null else giftId,
                                            tvName.text.toString(),
                                            tvPrice!!.text.toString().toInt(),
                                            profileUserId,
                                            userId,
                                            tvLink.text.toString(),
                                            postPrivacy
                                        )
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@GiftPageActivity,
                                        "Update failed, please try again",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                var memberCount: Int = gift.getInt("member_count")
                                try {
                                    memberId = gift.getInt("member_id")
                                } catch (e: Exception) {
                                    memberId = null
                                }
                                if (memberId != null && memberId != 0) {
                                    db.leaveGift(memberId!!)
                                    memberCount -= 1
                                    memberId = null
                                } else {
                                    val memberSet: ResultSet = db.joinGift(userId, giftId)
                                    if (memberSet.next()) {
                                        memberId = memberSet.getInt(1)
                                    }
                                    memberCount += 1
                                }
                                withContext(Dispatchers.Main) {
                                    updateJoinButtonColor(
                                        btnJoin,
                                        memberId,
                                        gift.getInt("owner_id")
                                    )
                                    tvMemberCount.text = memberCount.toString()
                                }
                            }
                        }
                    }
                }
            }
        }

        /*giftPageCommentsRv = findViewById(R.id.rvGiftPageComments)
        giftPageCommentsRv.layoutManager = LinearLayoutManager(giftPageCommentsRv.context)
        giftPageCommentsRv.setHasFixedSize(true)*/

    }

    fun updateJoinButtonColor(btnJoin: Button, memberId: Int?, ownerId: Int) {
        if (ownerId == userId) {
            btnJoin.text = "Änderungen speichern"
            btnJoin.setBackgroundColor(Color.argb(255, 0, 191, 255))
        } else if(memberId!=null && memberId!=0) {
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
        b.putInt("id", profileUserId)
        intent.putExtras(b)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivityIfNeeded(intent, 0)
    }

    private fun spinnerPostPrivacy(){
        val spinner: Spinner = findViewById(R.id.spPostPrivacy)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.post_privacy_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
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