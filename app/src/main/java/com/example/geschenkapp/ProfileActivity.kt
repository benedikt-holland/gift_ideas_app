package com.example.geschenkapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.geschenkapp.databinding.ActivityProfileBinding
import kotlinx.coroutines.*
import java.util.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.FileNotFoundException
import java.lang.NullPointerException
import java.sql.SQLException

class ProfileActivity : AppCompatActivity() {
    lateinit var db: DbConnector
    private lateinit var rvProfileFeed: RecyclerView
    private lateinit var profileFeedAdapter: ProfileFeedAdapter
    private lateinit var binding: ActivityProfileBinding
    //For thread creation
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //Overwrite activity content with new user data when resuming
    override fun onResume() {
        super.onResume()
        //Check internet connection
        if (!checkForInternet(this)) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        setContentView(R.layout.activity_profile)
        setSupportActionBar(findViewById(R.id.toolbar))
        //Initiate Actionbar with back button
        supportActionBar?.apply {
            title = resources.getString(R.string.tab_profile)
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        //Get userdata and database connector
        val user = LoginHolder.getInstance().user
        db = DbHolder.getInstance().db
        val userId = user.getInt("id")

        rvProfileFeed = findViewById(R.id.rvGiftFeed)
        rvProfileFeed.layoutManager = LinearLayoutManager(rvProfileFeed.context)
        rvProfileFeed.setHasFixedSize(true)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        //Get userId
        val b: Bundle? = intent.extras
        val friendUserId = b?.getInt("id") ?: userId
        intent.removeExtra("id")

        //Show settings button for personal profile and add friend button for stranger profile
        val btnSettings: ImageButton = findViewById(R.id.btnSettings)
        val btnAddFriend: ImageButton = findViewById(R.id.btnAddFriend)
        val fabAddGift: FloatingActionButton = findViewById(R.id.fabAddGift)
        if (friendUserId == userId) {
            btnSettings.visibility = View.VISIBLE
            btnAddFriend.visibility = View.GONE
        } else {
            btnSettings.visibility = View.GONE
            btnAddFriend.visibility = View.VISIBLE
        }

        //Listener for settings button
        btnSettings.setOnClickListener {
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }

        //Listener for add Gift button
        fabAddGift.setOnClickListener {
            val intent = Intent(this, GiftpageActivity::class.java)
            val b2 = Bundle()
            b2.putInt("profileUserId", friendUserId)
            intent.putExtras(b2)
            startActivity(intent)
        }

        //Register bottom nav bar
        useBottomNavBar()

        //Fill textview with userdata
        val tvName: TextView = findViewById(R.id.tvName)
        val tvDateofbirth: TextView = findViewById(R.id.tvProfileDateofbirth)
        val ivProfilepicture: ImageView = findViewById(R.id.ivProfilepicture)

        try {
            uiScope.launch(Dispatchers.IO) {
                //Load user data
                val profileUser = db.getUser(userId, friendUserId)
                profileUser.next()

                //Set friend button status
                var isFriend: Boolean = profileUser.getInt("is_friend") == 1
                val notificationId: Int = db.getNotificationId(0, friendUserId, userId)
                var isNotified: Boolean = notificationId != 0
                updateAddFriendButtonColor(btnAddFriend, isFriend, isNotified)

                try {
                    withContext(Dispatchers.Main) {
                        tvName.text = if (profileUser.getString("last_name") != null) {
                            profileUser.getString("first_name") + " " + profileUser.getString("last_name")
                        } else {
                            profileUser.getString("first_name")
                        }
                        try {
                            tvDateofbirth.text = profileUser.getString("date_of_birth")
                        } catch (e: SQLException) {
                            tvDateofbirth.visibility = View.INVISIBLE
                            println("User privacy settings hides date of birth")
                        }
                        if ((userId!=friendUserId) && (isFriend || profileUser.getInt("profile_privacy") == 0)) {
                            fabAddGift.visibility = View.VISIBLE
                        } else {
                            fabAddGift.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    println("Unable to set user data")
                }


                //Listener for friend add button
                btnAddFriend.setOnClickListener {
                    uiScope.launch(Dispatchers.IO) {
                        if (!isFriend && profileUser.getInt("profile_privacy") == 0) {
                            //No friends request for public profiles
                            db.addFriend(friendUserId, userId)
                            isFriend = true
                        } else if (!isFriend && !isNotified) {
                            //Send friends request
                            db.addNotification(0, friendUserId, userId)
                            isNotified = true
                        } else if (!isFriend && isNotified) {
                            //Cancel friends request
                            db.removeNotificationById(notificationId)
                            isNotified = false
                        } else {
                            //Remove friend
                            db.removeFriend(friendUserId, userId)
                            isFriend = false
                            isNotified = false
                        }

                        withContext(Dispatchers.Main) {
                            updateAddFriendButtonColor(btnAddFriend, isFriend, isNotified)
                        }
                    }
                }

                //Only load gift feed if profile is public or profile user is friends with user
                if (profileUser.getInt("profile_privacy") == 0 || isFriend) {
                    loadGiftFeed(userId, friendUserId)
                }
                setNotificationNumber(userId)

                //Only load profile picture if profile privacy is 2 or below
                if (profileUser.getInt("profile_privacy") < 3) {
                    try {
                        val inputStream = assets.open("config.properties")
                        val props = Properties()
                        props.load(inputStream)

                        //
                        val profilePictureFileName = profileUser.getString("profile_picture")
                        //


                        val auth = props.getProperty("API_AUTH", "")
                        val downloadUri =
                            props.getProperty("API_DOWNLOAD", "") + profilePictureFileName
                        inputStream.close()

                        val imageConnector = ImageConnector()
                        val profilePicture = imageConnector.getImage(downloadUri, auth)
                        withContext(Dispatchers.Main) {
                            ivProfilepicture.setImageBitmap(profilePicture)
                        }
                    } catch (e: FileNotFoundException) {
                        System.err.println("Missing config.properties file in app/src/main/assets/ containing database credentials")
                    } catch (e: NullPointerException) {
                        println("User has no profile picture")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.actionShare -> {
                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Load giftfeed of selected user and load into recyclerview
    private suspend fun loadGiftFeed(userId: Int, friendUserId: Int) {
        val giftFeed = if (userId != friendUserId) {
            db.getGiftFeedByUserId(userId, friendUserId)
        } else {
            db.getGiftFeedByMemberId(userId)
        }
        val giftFeedArray = unloadResultSet(giftFeed)
        withContext(Dispatchers.Main) {
            if (rvProfileFeed.adapter==null) {
                profileFeedAdapter = ProfileFeedAdapter(giftFeedArray)
                rvProfileFeed.adapter = profileFeedAdapter
            } else {
                profileFeedAdapter.updateData(giftFeedArray)
            }
        }
    }

    //Update Color of add friend button according to friendship status
    private fun updateAddFriendButtonColor(btnAddFriend: ImageButton, isFriend: Boolean, isNotified: Boolean) {
        when {
            isFriend -> {
                btnAddFriend.setColorFilter(Color.argb(255, 50, 205, 50))
            }
            isNotified -> {
                btnAddFriend.setColorFilter(Color.argb(255, 0, 191, 255))
            }
            else -> {
                btnAddFriend.setColorFilter(Color.argb(255, 0, 0, 0))
            }
        }
    }

    //Bottom navigation bar on tab profile
    private fun useBottomNavBar() {
        val menuBottomNavBar: BottomNavigationView = findViewById(R.id.mBottomNavigation)
        menuBottomNavBar.selectedItemId = R.id.ic_bottom_nav_profile
        menuBottomNavBar.setOnItemSelectedListener { item ->
            Log.d("ProfileActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.ic_bottom_nav_notifications -> {
                    Log.d("NotificationActivity", "notification")
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }
                R.id.ic_bottom_nav_profile -> {}
                else -> {
                    Log.d("ProfileActivity", "item not found")
                }
            }
            true

        }
    }

    private suspend fun setNotificationNumber(userId: Int){
        val count = db.getNotificationCount(userId)
        withContext(Dispatchers.Main) {
            binding.mBottomNavigation.getOrCreateBadge(R.id.ic_bottom_nav_notifications).apply {
                number = count
                isVisible = count != 0
            }
        }
    }
}
