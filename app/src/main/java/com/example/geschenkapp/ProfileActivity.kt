package com.example.geschenkapp

import android.content.Intent
import android.graphics.Bitmap
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
import java.sql.ResultSet
import java.sql.SQLException

/*Possible tabs
var initTabArray = arrayOf(
    "Geschenke",
    "Wunschliste",
    "Events",
    "Freunde"
)*/

class ProfileActivity : AppCompatActivity() {
    private lateinit var menuBottomNavBar: BottomNavigationView
    private lateinit var binding: ActivityProfileBinding
    private lateinit var profilePicture: Bitmap
    lateinit var user: ResultSet
    private var userId: Int = -1
    lateinit var db: DbConnector
    private lateinit var rvProfileFeed: RecyclerView
    private lateinit var profileFeedAdapter: ProfileFeedAdapter
    private var friendUserId: Int = -1
    private lateinit var profileUser: ResultSet

    //Overwrite activity content with new user data when resuming
    override fun onResume() {
        super.onResume()

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
        user = LoginHolder.getInstance().user
        db = DbHolder.getInstance().db
        userId = user.getInt("id")

        rvProfileFeed = findViewById(R.id.rvGiftFeed)
        rvProfileFeed.layoutManager = LinearLayoutManager(rvProfileFeed.context)
        rvProfileFeed.setHasFixedSize(true)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        //Get userId
        val b: Bundle? = intent.extras
        if (b != null) {
            friendUserId = b.getInt("id")
        }

        //Show settings button for personal profile and add friend button for stranger profile
        val btnSettings: ImageButton = findViewById(R.id.btnSettings)
        val btnAddFriend: ImageButton = findViewById(R.id.btnAddFriend)
        if (friendUserId == userId) {
            //tabArray = initTabArray.slice(1..3).toTypedArray()
            btnSettings.visibility = View.VISIBLE
            btnAddFriend.visibility = View.GONE
        } else {
            //tabArray = initTabArray.slice(0..2).toTypedArray()
            btnSettings.visibility = View.GONE
            btnAddFriend.visibility = View.VISIBLE
        }
        useBottomNavBar()
        /*Set tabs for profile page
        //Hide 'friends' tab for stranger profile and 'gifts' tab for personal profile
        tabLayout = findViewById(R.id.profileTabLayout)
        viewPager = findViewById(R.id.profileViewPager)
        val adapter =
            ProfileTabAdapter(supportFragmentManager, lifecycle, userId, friendUserId)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabArray[position]
        }.attach()
        */

        //Fill textview with userdata
        val tvName: TextView = findViewById(R.id.tvName)
        val tvDateofbirth: TextView = findViewById(R.id.tvProfileDateofbirth)
        val ivProfilepicture: ImageView = findViewById(R.id.ivProfilepicture)

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {

            //Load user data
            try {
                profileUser = db.getUser(userId, friendUserId)
            } catch (e: Exception) {
                println("Unable to load user data")
                e.printStackTrace()
            }
            try {
                if (!profileUser.isLast) {
                    profileUser.next()
                }

                //Set friend button status
                var isFriend: Boolean = profileUser.getInt("is_friend") == 1
                val notificationId: Int = db.getNotificationId(0, friendUserId, userId)
                var isNotified: Boolean = notificationId!=0
                updateAddFriendButtonColor(btnAddFriend, isFriend, isNotified)

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
                    val btnAddGift: FloatingActionButton = findViewById(R.id.fabAddGift)
                    if (isFriend || profileUser.getInt("profile_privacy")==0) {
                        btnAddGift.visibility = View.VISIBLE
                    } else {
                        btnAddGift.visibility = View.GONE
                    }
                }

                //Listener for friend add button
                btnAddFriend.setOnClickListener {
                    uiScope.launch(Dispatchers.IO) {
                        if (!isFriend && profileUser.getInt("profile_privacy") == 0) {
                            //No friends request for public profiles
                            db.addFriend(friendUserId, userId)
                            isFriend = true
                            isNotified = false
                        } else if (!isFriend && !isNotified) {
                            //Send friends request
                            db.addNotification(0, friendUserId, userId)
                            isNotified = true
                        } else if (!isFriend && isNotified){
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
            } catch (e: Exception) {
                println("Unable to set user data")
            }

            //Load recyclerview with gift feed
            loadGiftFeed(userId, friendUserId)
            setNotificationNumber()

            //Load profile picture
            try {
                val inputStream = assets.open("config.properties")
                val props = Properties()
                props.load(inputStream)

                //
                val profilePictureFileName = profileUser.getString("profile_picture")
                //


                val auth = props.getProperty("API_AUTH", "")
                val downloadUri = props.getProperty("API_DOWNLOAD", "") + profilePictureFileName
                inputStream.close()

                val imageConnector = ImageConnector()
                profilePicture = imageConnector.getImage(downloadUri, auth)
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
        getButtonClick()
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

    //Register buttons
    private fun getButtonClick() {
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }
        val btnAddGift: FloatingActionButton = findViewById(R.id.fabAddGift)
        btnAddGift.setOnClickListener {
            val intent = Intent(this, GiftpageActivity::class.java)
            val b = Bundle()
            b.putInt("profileUserId", friendUserId)
            intent.putExtras(b)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
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
        menuBottomNavBar = findViewById(R.id.bottomNavigation)
        menuBottomNavBar.selectedItemId = R.id.ic_bottom_nav_profile
        menuBottomNavBar.setOnItemSelectedListener { item ->
            Log.d("ProfileActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_notifications -> {
                    Log.d("NotificationActivity", "notification")
                    val intent = Intent(this, NotificationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_profile -> {}
                else -> {
                    Log.d("ProfileActivity", "item not found")
                }
            }
            true

        }
    }

    private suspend fun setNotificationNumber(){
        val count = db.getNotificationCount(user.getInt("id"))
        withContext(Dispatchers.Main) {
            binding.bottomNavigation.getOrCreateBadge(R.id.ic_bottom_nav_notifications).apply {
                number = count
                isVisible = count != 0
            }
        }
    }
}
