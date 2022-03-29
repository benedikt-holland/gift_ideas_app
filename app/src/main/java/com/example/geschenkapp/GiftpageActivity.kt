package com.example.geschenkapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.sql.ResultSet

//Class for gift detail page
class GiftpageActivity  : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private var giftId: Int = -1
    private var memberId: Int? = null
    private var userId: Int = -1
    private var profileUserId: Int = -1


    @SuppressLint("SetTextI18n")
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
        user = LoginHolder.getInstance().user
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
                val gift = db.getGiftById(userId, giftId)
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
                        title = if (gift.getString("user_last_name") != null) {
                            gift.getString("user_first_name") + " " + gift.getString("user_last_name")
                        } else {
                            gift.getString("user_first_name")
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
                        uiScope.launch(Dispatchers.IO) {
                            //User is owner of gift
                            if (userId == gift.getInt("owner_id")) {
                                val profilePrivacyArray: Array<String> =
                                    resources.getStringArray(R.array.profile_privacy_array)
                                var postPrivacy = 0
                                for (i in profilePrivacyArray.indices) {
                                    if (profilePrivacyArray[i].contains(spPrivacy.selectedItem.toString())) {
                                        postPrivacy = i
                                    }
                                }
                                //Update database according to input data
                                try {
                                    db.updateGift(
                                        giftId,
                                        tvName.text.toString(),
                                        tvPrice.text.toString().toInt(),
                                        profileUserId,
                                        userId,
                                        tvLink.text.toString(),
                                        postPrivacy
                                    )
                                    db.notifiyAll(2, user.getInt("id"), giftId)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@GiftpageActivity,
                                            getString(R.string.saved),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@GiftpageActivity,
                                            "Update failed, please try again",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                // If not owner
                            } else {
                                var memberCount: Int = gift.getInt("member_count")
                                memberId = try {
                                    gift.getInt("member_id")
                                } catch (e: Exception) {
                                    null
                                }
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
            uiScope.launch(Dispatchers.IO) {
                //User is owner of gift
                    val profilePrivacyArray: Array<String> =
                        resources.getStringArray(R.array.profile_privacy_array)
                    var postPrivacy = 0
                    for (i in profilePrivacyArray.indices) {
                        if (profilePrivacyArray[i].contains(spPrivacy.selectedItem.toString())) {
                            postPrivacy = i
                        }
                    }
                    //Update database according to input data
                    try {
                        db.updateGift(
                            null,
                            tvName.text.toString(),
                            tvPrice.text.toString().toInt(),
                            profileUserId,
                            userId,
                            tvLink.text.toString(),
                            postPrivacy
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@GiftpageActivity,
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
    private fun updateJoinButtonColor(btnJoin: Button, memberId: Int?, ownerId: Int, isNotified: Boolean = false) {
        if (ownerId == userId) {
            btnJoin.text = getString(R.string.save_changes)
            btnJoin.setBackgroundColor(Color.argb(255, 0, 191, 255))
        } else if(memberId!=null && memberId!=0) {
            btnJoin.text = getString(R.string.leave)
            btnJoin.setBackgroundColor(Color.argb(255, 255, 0, 0))
        } else if(!isNotified) {
            btnJoin.text = getString(R.string.join)
            btnJoin.setBackgroundColor(Color.argb(255, 50, 205, 50))
        } else {
            btnJoin.text = getString(R.string.requested)
            btnJoin.setBackgroundColor(Color.argb(255, 0, 191, 255))
        }
    }

    //Call profile activity with userId
    override fun onBackPressed() {
        val intent = Intent(this, ProfileActivity::class.java)
        val b = Bundle()
        b.putInt("id", profileUserId)
        intent.putExtras(b)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
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
        val tvDeleteGift = findViewById<TextView>(R.id.tvDeleteGift)
        tvDeleteGift.setOnClickListener {
            val builder = AlertDialog.Builder(this@GiftpageActivity)
            builder.setMessage(R.string.delete_gift_dialog)
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { _, _ ->
                    // Delete selected note from database
                    TODO()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
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