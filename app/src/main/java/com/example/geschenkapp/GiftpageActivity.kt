package com.example.geschenkapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.sql.ResultSet
import java.sql.SQLException

//Class for gift detail page
class GiftpageActivity  : AppCompatActivity() {
    lateinit var user: ResultSet
    private var db = DbConnector()
    private var profileUserId = 0
    //For thread creation
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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

        val userId = user.getInt("id")
        var giftId = 0

        //Extract giftId and user id of selected profile from extras
        val b: Bundle? = intent.extras
        if (b != null) {
            //giftId returns 0 if none is specified
            //No gift idea means a new gift is being created
            giftId = b.getInt("id")
            profileUserId = b.getInt("profileUserId")
        }
        intent.removeExtra("id")
        intent.removeExtra("profileUserId")

        //Register privacy spinner
        spinnerPostPrivacy()

        //If gift exists
        if (giftId != 0) {
            try {
                uiScope.launch(Dispatchers.IO) {
                    //Get gift data
                    val gift = db.getGiftById(userId, giftId)
                    gift.next()
                    val ownerId = try {
                        gift.getInt("owner_id")
                    } catch (e: SQLException) {
                        e.printStackTrace()
                        0
                    }
                    //Check if join request has already been sent
                    val notificationId = db.getNotificationId(1, ownerId, userId, giftId)
                    withContext(Dispatchers.Main) {
                        fillTextViews(gift, userId)
                        //If user is not owner
                        if (userId != ownerId) {
                            registerMemberButtons(gift, notificationId)
                        } else {
                            registerOwnerButton(profileUserId, giftId)
                        }
                    }
                }
            } catch (e:SQLException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        //Gift doesn't exist -> Create new gift
        } else {
            registerOwnerButton(profileUserId, null)
        }

    }

    //Fills giftpage textviews with data from result set
    private fun fillTextViews(gift: ResultSet, userId: Int) {
        val tvName: TextView = findViewById(R.id.tvName)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvLink: TextView = findViewById(R.id.tvLink)
        val tvOwner: TextView = findViewById(R.id.tvOwner)
        val spPrivacy: Spinner = findViewById(R.id.spGiftpagePostPrivacy)
        val tvMemberCount: TextView = findViewById(R.id.tvMemberCount)
        val tvDeleteGift: TextView = findViewById(R.id.tvDeleteGift)

        tvOwner.text = if (gift.getString("owner_last_name") != null) {
            gift.getString("owner_first_name") + " " + gift.getString("owner_last_name")
        } else {
            gift.getString("owner_first_name")
        }
        tvName.text = gift.getString("title")
        tvPrice.text = gift.getString("price")
        tvLink.text = gift.getString("gift_link")
        spPrivacy.setSelection(gift.getInt("post_privacy"))
        tvMemberCount.text = gift.getString("member_count")

        //Disable Edit Views if user is not the owner
        //Register delete gift option for owner
        if (gift.getInt("owner_id") == userId) {
            tvName.isEnabled = true
            tvPrice.isEnabled = true
            tvLink.isEnabled = true
            spPrivacy.isEnabled = true
            tvDeleteGift.visibility = View.VISIBLE
            registerDeleteGift(userId, gift.getInt("id"))
        } else {
            tvName.isEnabled = false
            tvPrice.isEnabled = false
            tvLink.isEnabled = false
            spPrivacy.isEnabled = false
            tvDeleteGift.visibility = View.GONE
        }

        //Set name of profile user onto actionbar
        supportActionBar?.apply {
            title = if (gift.getString("user_last_name") != null) {
                getString(R.string.text_for) + " " + gift.getString("user_first_name") + " " + gift.getString("user_last_name")
            } else {
                getString(R.string.text_for) + " " + gift.getString("user_first_name")
            }
        }
    }

    private fun registerMemberButtons(gift: ResultSet, notificationId: Int) {
        val btnJoin: Button = findViewById(R.id.btnJoin)
        val btnRequested: Button = findViewById(R.id.btnRequested)
        val btnLeave: Button = findViewById(R.id.btnLeave)
        val tvMemberCount: TextView = findViewById(R.id.tvMemberCount)
        var newNotificationId = notificationId

        //Edit Button not needed for members
        findViewById<Button?>(R.id.btnEdit).visibility = View.GONE

        val memberId = try {
            gift.getInt("member_id")
        } catch (e: Exception) {
            0
        }
        if (memberId!=0) {
            //If user is member show leave button
            btnJoin.visibility = View.GONE
            btnRequested.visibility = View.GONE
            btnLeave.visibility = View.VISIBLE
        } else if (notificationId>0){
            //If join request has been sent show requested button
            btnJoin.visibility = View.GONE
            btnRequested.visibility = View.VISIBLE
            btnLeave.visibility = View.GONE
        } else {
            //Else show join button
            btnJoin.visibility = View.VISIBLE
            btnRequested.visibility = View.GONE
            btnLeave.visibility = View.GONE
        }

        //Send notification to owner to request join
        btnJoin.setOnClickListener {
            try {
                uiScope.launch(Dispatchers.IO) {
                    newNotificationId = db.addNotification(
                        1,
                        gift.getInt("owner_id"),
                        user.getInt("id"),
                        gift.getInt("id")
                  )
                    withContext(Dispatchers.Main) {
                        btnJoin.visibility = View.GONE
                        btnRequested.visibility = View.VISIBLE
                        btnLeave.visibility = View.GONE
                    }
                }
            }  catch (e: SQLException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }

        //Remove notification for owner
        btnRequested.setOnClickListener {
            try {
                uiScope.launch(Dispatchers.IO) {
                    db.removeNotificationById(newNotificationId)
                    withContext(Dispatchers.Main) {
                        btnJoin.visibility = View.VISIBLE
                        btnRequested.visibility = View.GONE
                        btnLeave.visibility = View.GONE
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }

        //Leave gift and send notification to owner
        btnLeave.setOnClickListener {
            try {
                uiScope.launch(Dispatchers.IO) {
                    db.leaveGift(memberId)
                    withContext(Dispatchers.Main) {
                        btnJoin.visibility = View.VISIBLE
                        btnRequested.visibility = View.GONE
                        btnLeave.visibility = View.GONE
                        tvMemberCount.text = (tvMemberCount.text.toString().toInt() - 1).toString()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
            try {
                db.addNotification(4, user.getInt("id"), gift.getInt("owner_id"), gift.getInt("id"))
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    //Register edit button
    //giftId = null will create a new gift
    //Needs to be recalled when gift has been created
    @SuppressLint("SetTextI18n")
    private fun registerOwnerButton(profileUserId: Int, giftId: Int?) {
        val btnEdit: Button = findViewById(R.id.btnEdit)
        btnEdit.visibility = View.VISIBLE

        val tvName: TextView = findViewById(R.id.tvName)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvLink: TextView = findViewById(R.id.tvLink)
        val spPrivacy: Spinner = findViewById(R.id.spGiftpagePostPrivacy)
        val tvMemberCount: TextView = findViewById(R.id.tvMemberCount)
        val tvDeleteGift: TextView = findViewById(R.id.tvDeleteGift)

        //Member buttons not needed for owner
        findViewById<Button?>(R.id.btnJoin).visibility = View.GONE
        findViewById<Button?>(R.id.btnRequested).visibility = View.GONE
        findViewById<Button?>(R.id.btnLeave).visibility = View.GONE

        //Only show delete button if gift exists
        tvDeleteGift.visibility = if (giftId != null) {
            View.VISIBLE
        } else {
            tvName.text = user.getString("first_name") + " " + user.getString("last_name")
            View.GONE
        }

        btnEdit.setOnClickListener {
            val userId = user.getInt("id")
            //Get post privacy setting from spinner and convert to id
            //Default Privacy 0
            val profilePrivacyArray: Array<String> =
                resources.getStringArray(R.array.profile_privacy_array)
            var postPrivacy = 0
            try {
                for (i in profilePrivacyArray.indices) {
                    if (profilePrivacyArray[i].contains(spPrivacy.selectedItem.toString())) {
                        postPrivacy = i
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Extract data from Editfields and apply default values if empty or faulty
            val title = try {
                tvName.text.toString()
            } catch (e: Exception) {
                getString(R.string.giftidea)
            }
            val price = try {
                tvPrice.text.toString().toInt()
            } catch (e: Exception) {
                0
            }
            val link = try {
                tvLink.text.toString()
            } catch (e: Exception) {
                ""
            }

            try {
                uiScope.launch(Dispatchers.IO) {
                    //Update database according to input data
                    //If giftId = null a new gift will be created
                    val newGiftId = db.updateGift(
                        giftId,
                        title,
                        price,
                        profileUserId,
                        userId,
                        link,
                        postPrivacy
                    )
                    if (giftId != null) {
                        //Notify all members about change
                        try {
                            db.notifiyAll(2, userId, giftId)
                        } catch (e: SQLException) {
                            e.printStackTrace()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@GiftpageActivity,
                            getString(R.string.successful_save),
                            Toast.LENGTH_SHORT
                        ).show()
                        //If new gift was created
                        if (giftId==null) {
                            //Increment member counter to 1
                            tvMemberCount.text = "1"
                            //Re register edit button with new gift Id
                            registerOwnerButton(profileUserId, newGiftId)
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Toast.makeText(
                    this@GiftpageActivity,
                    getString(R.string.error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //Call profile activity with userId
    override fun onBackPressed() {
        val intent = Intent(this, ProfileActivity::class.java)
        val b = Bundle()
        b.putInt("id", profileUserId)
        intent.putExtras(b)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    //Set spinner data
    private fun spinnerPostPrivacy(){
        val spPostPrivacy: Spinner = findViewById(R.id.spGiftpagePostPrivacy)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.post_privacy_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spPostPrivacy.adapter = adapter
        }
    }

    private fun registerDeleteGift(userId: Int, giftId: Int){
        val tvDeleteGift = findViewById<TextView>(R.id.tvDeleteGift)
        tvDeleteGift.setOnClickListener {
            val builder = AlertDialog.Builder(this@GiftpageActivity)
            builder.setMessage(R.string.delete_gift_dialog)
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { _, _ ->
                    val viewModelJob = SupervisorJob()
                    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                    uiScope.launch(Dispatchers.IO) {
                        db.deleteGift(userId, giftId)
                        withContext(Dispatchers.Main) {
                            finish()
                        }
                    }
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }
}