package com.example.geschenkapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.sql.ResultSet
import java.sql.SQLException

//Class for gift detail page
class GiftPageActivity  : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private var giftId: Int = -1
    private var memberId: Int? = null
    private var userId: Int = -1
    private var profileUserId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giftpage)
        //Set up action bar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        //Get dataholders with userdata and database connector
        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        //Extract giftId and user id of selected profile
        val b: Bundle? = intent.extras
        if (b != null) {
            giftId = b.getInt("id")
            profileUserId = b.getInt("profileUserId")
        }
        //giftId returns 0 if none is specified
        //No gift idea means a new gift is being created

        val tvName: TextView = findViewById(R.id.tvName)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvLink: TextView = findViewById(R.id.tvLink)
        val tvOwner: TextView = findViewById(R.id.tvOwner)
        val spPrivacy: Spinner = findViewById(R.id.spGiftpagePostPrivacy)
        val tvMemberCount: TextView = findViewById(R.id.tvMemberCount)
        val btnJoin: Button = findViewById(R.id.btnJoin)

        userId = user.getInt("id")
        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            if (giftId > 0) {
                //Get gift data from database
                var gift = db.getGiftById(userId, giftId)
                gift.next()
                val notificationId: Int = db.getNotificationId(
                    1,
                    gift.getInt("owner_id"),
                    user.getInt("id"),
                    giftId
                )
                withContext(Dispatchers.Main) {
                    //Set text view content
                    tvName.text = gift.getString("title")
                    tvPrice.text = gift.getString("price")
                    tvLink.text = gift.getString("gift_link")
                    if (gift.getString("owner_last_name") != null) {
                        tvOwner.text =
                            gift.getString("owner_first_name") + " " + gift.getString("owner_last_name")
                    } else {
                        tvOwner.text = gift.getString("owner_first_name")
                    }
                    //Disable Edit Views if user is not the owner
                    if (gift.getInt("owner_id") == userId) {
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
                    //Set name of profile user onto actionbar
                    supportActionBar?.apply {
                        if (gift.getString("user_last_name") != null) {
                            title =
                                gift.getString("user_first_name") + " " + gift.getString("user_last_name")
                        } else {
                            title = gift.getString("user_first_name")
                        }
                    }
                    var isNotified: Boolean = notificationId != 0

                    //Preselect privacy settings spinner
                    spPrivacy.setSelection(gift.getInt("post_privacy"))
                    tvMemberCount.text = gift.getString("member_count")//Update Join button:
                    // 'Leave' if member, 'Join' if no member, 'Save changes' if owner
                        updateJoinButtonColor(
                            btnJoin,
                            gift.getInt("member_id"),
                            gift.getInt("owner_id"),
                            isNotified
                        )
                    btnJoin.setOnClickListener {
                        val viewModelJob = SupervisorJob()
                        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                        uiScope.launch(Dispatchers.IO) {
                            //User is owner of gift
                            if (userId == gift.getInt("owner_id")) {
                                val profilePrivacyArray: Array<String> =
                                    resources.getStringArray(R.array.profile_privacy_array)
                                var postPrivacy: Int = 0
                                for (i in profilePrivacyArray.indices) {
                                    if (profilePrivacyArray[i].contains(spPrivacy.selectedItem.toString())) {
                                        postPrivacy = i
                                    }
                                }
                                //Update dabase according to input data
                                try {
                                    db.updateGift(
                                        giftId,
                                        tvName.text.toString(),
                                        tvPrice!!.text.toString().toInt(),
                                        profileUserId,
                                        userId,
                                        tvLink.text.toString(),
                                        postPrivacy
                                    )
                                    db.notifiyAll(2, user.getInt("id"), giftId)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@GiftPageActivity,
                                            getString(R.string.saved),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@GiftPageActivity,
                                            "Update failed, please try again",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                // If not owner
                            } else {
                                var memberCount: Int = gift.getInt("member_count")
                                try {
                                    memberId = gift.getInt("member_id")
                                } catch (e: Exception) {
                                    memberId = null
                                }
                                val notificationId: Int = db.getNotificationId(1, gift.getInt("owner_id"), user.getInt("id"), giftId)
                                var isNotified: Boolean = notificationId != 0
                                //When user is a member of gift -> Leave
                                var joined: Boolean = memberId!=0 && memberId!=null
                                if (joined) {
                                    db.leaveGift(memberId!!)
                                    memberCount -= 1
                                    joined = false
                                    isNotified = false
                                    db.addNotification(4, user.getInt("id"), gift.getInt("owner_id"), giftId)
                                    //When user is not a member of gift -> Request Join
                                } else if (!isNotified) {
                                    db.addNotification(1, gift.getInt("owner_id"), user.getInt("id"), giftId)
                                    isNotified = true
                                } else {
                                    db.removeNotificationById(notificationId)
                                    isNotified = false
                                }
                                withContext(Dispatchers.Main) {
                                    //Update Text and color of join button
                                    updateJoinButtonColor(
                                        btnJoin,
                                        if(joined) 1 else 0,
                                        gift.getInt("owner_id"),
                                        isNotified
                                    )
                                    //Update member count
                                    tvMemberCount.text = memberCount.toString()
                                }
                            }
                        }
                    }
                }
            }
        }

        //When creating a new gift
        if(giftId < 1) {
        updateJoinButtonColor(btnJoin, null, userId)
        //Set listener for create button
        btnJoin.setOnClickListener {
            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {
                //User is owner of gift
                    val profilePrivacyArray: Array<String> =
                        resources.getStringArray(R.array.profile_privacy_array)
                    var postPrivacy: Int = 0
                    for (i in profilePrivacyArray.indices) {
                        if (profilePrivacyArray[i].contains(spPrivacy.selectedItem.toString())) {
                            postPrivacy = i
                        }
                    }
                    //Update dabase according to input data
                    try {
                        db.updateGift(
                            null,
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
                }
            }
        }

        /*giftPageCommentsRv = findViewById(R.id.rvGiftPageComments)
        giftPageCommentsRv.layoutManager = LinearLayoutManager(giftPageCommentsRv.context)
        giftPageCommentsRv.setHasFixedSize(true)*/
        spinnerPostPrivacy()
        deleteGift()

    }

    //Sets Join button text and color according to
    // Red 'Leave' if member, Green 'Join' if no member, Blue 'Save changes' if owner
    fun updateJoinButtonColor(btnJoin: Button, memberId: Int?, ownerId: Int, isNotified: Boolean = false) {
        if (ownerId == userId) {
            btnJoin.text = "Änderungen speichern"
            btnJoin.setBackgroundColor(Color.argb(255, 0, 191, 255))
        } else if(memberId!=null && memberId!=0) {
            btnJoin.text = "Verlassen"
            btnJoin.setBackgroundColor(Color.argb(255, 255, 0, 0))
        } else if(!isNotified) {
            btnJoin.text = "Teilnehmen"
            btnJoin.setBackgroundColor(Color.argb(255, 50, 205, 50))
        } else {
            btnJoin.text = "Angefragt"
            btnJoin.setBackgroundColor(Color.argb(255, 0, 191, 255))
        }
    }

    //Call profile activity with userId
    override fun onBackPressed() {
        var intent = Intent(this, ProfileActivity::class.java)
        var b = Bundle()
        b.putInt("id", profileUserId)
        intent.putExtras(b)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivityIfNeeded(intent, 0)
    }

    //Set spinner data
    private fun spinnerPostPrivacy(){
        val spinner: Spinner = findViewById(R.id.spGiftpagePostPrivacy)
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

    private fun deleteGift(){
        val tvDeleteGift = findViewById(R.id.tvDeleteGift) as TextView
        tvDeleteGift.setOnClickListener {
            val builder = AlertDialog.Builder(this@GiftPageActivity)
            builder.setMessage(R.string.deleteDialogGift)
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { dialog, id ->
                    // Delete selected note from database
                    TODO()
                }
                .setNegativeButton(R.string.no) { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
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