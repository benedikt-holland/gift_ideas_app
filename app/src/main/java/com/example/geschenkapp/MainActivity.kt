package com.example.geschenkapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.sql.ResultSet
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var user: ResultSet
    private lateinit var friendsFeed: ResultSet
    private lateinit var giftFeed: ResultSet
    lateinit var friendsFeedAdapter: FriendsFeedAdapter
    private lateinit var friendsFeedRv: RecyclerView
    private var db = DbConnector()
    private var userId: Int = -1
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get user data and database connector
        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                //Load friendsfeed and set recyclerview content
                userId = user.getInt("id")
                loadFriendsFeed(userId)
                setNotificationNumber()
            }  catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar()

        //Initiate and set content of friendsfeed recyclerview
        friendsFeedRv = findViewById(R.id.rvFriendsFeed)
        friendsFeedRv.layoutManager = LinearLayoutManager(friendsFeedRv.context)
        friendsFeedRv.setHasFixedSize(true)

        //Listener for clicking on recyclerview cards. Opens profile page of selected user
        binding.search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query!=null) {
                    try {
                        uiScope.launch(Dispatchers.IO) {
                                val profile: ResultSet = db.searchUser(userId, query)
                                withContext(Dispatchers.Main) {
                                    try {
                                        profile.next()
                                        val intent =
                                            Intent(this@MainActivity, ProfileActivity::class.java)
                                        val b = Bundle()
                                        b.putInt("id", profile.getInt("id"))
                                        intent.putExtras(b)
                                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                        startActivityIfNeeded(intent, 0)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Can't find user $query! Try searching for their email.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                    } catch (e: Exception) {}
                }
                return false
            }

            //Listen for searchbar changes
            override fun onQueryTextChange(newText: String?): Boolean {
                friendsFeedAdapter.filter.filter(newText)
                return false
            }

        })

        //getButtonClick()

    }

    //Will be called when returning from other activity
    override fun onResume() {
        super.onResume()
        db = DbHolder.getInstance().db


        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                //Reload friendsfeed on resume
                userId = user.getInt("id")
                loadFriendsFeed(userId)
                setNotificationNumber()
            }  catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //bottom navigation bar
        binding.bottomNavigation.selectedItemId = R.id.ic_bottom_nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            Log.d("MainActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_profile -> {
                    //Launch profile activity and pass id of logged user
                    val intent = Intent(this, ProfileActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", userId)
                    intent.putExtras(b)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_notifications -> {
                    //Launch notifications activity
                    Log.d("NotificationActivity", "notification")
                    val intent = Intent(this, NotificationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_home -> {}
                else -> {
                    Log.d("MainActivity", "item not found")
                }
            }
            true

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
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
        
//Not implemented yet
    suspend fun loadGiftFeed(userId: Int) {
        val giftList = ArrayList<ArrayList<String>>()
        giftFeed = db.getGiftFeedByMemberId(userId)
        while(giftFeed.next()) {
            val row = ArrayList<String>()
            for (i in 1..8) {
                row.add(giftFeed.getString(i))
            }
            giftList.add(row)
        }
        withContext(Dispatchers.Main) {
            friendsFeedAdapter = FriendsFeedAdapter(giftList)
            friendsFeedRv.adapter = friendsFeedAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun loadFriendsFeed(userId: Int) {
        friendsFeed = db.getFriendsFeed(userId)
        val friendsFeedArray = unloadResultSet(friendsFeed)
        withContext(Dispatchers.Main) {
            friendsFeedAdapter = FriendsFeedAdapter(friendsFeedArray)
            friendsFeedRv.adapter = friendsFeedAdapter
            friendsFeedAdapter.notifyDataSetChanged()
        }
    }

    /*Register Actionbar buttons, not implemented yet
    private fun getButtonClick(){
        val btnStar = findViewById(R.id.btnFavorites) as Button
        btnStar.setOnClickListener {
            Toast.makeText(this, "favorites", Toast.LENGTH_SHORT).show()
        }
        val btnGift = findViewById(R.id.btnGeschenk) as Button
        btnGift.setOnClickListener {
            Toast.makeText(this, "gift", Toast.LENGTH_SHORT).show()
        }
    }*/

    private suspend fun setNotificationNumber(){
        val count = db.getNotificationCount(userId)
        withContext(Dispatchers.Main) {
            binding.bottomNavigation.getOrCreateBadge(R.id.ic_bottom_nav_notifications).apply {
                number = count
                isVisible = count != 0
            }
        }
    }

    private fun setSupportActionBar(){
        //toolbar
        supportActionBar?.apply {
            title = try {
                getString(R.string.welcome) + " " + user.getString("first_name")
            } catch (e: Exception) {
                e.printStackTrace()
                "Home"
            }
        }
    }
}

//Helper function for turning SQL ResultSets into Arrays for use in recyclerview
fun unloadResultSet(resultSet: ResultSet): ArrayList<ArrayList<String>> {
    val resultSetArray = ArrayList<ArrayList<String>>()
    while(resultSet.next()) {
        val row = ArrayList<String>()
        for (i in 1 until resultSet.metaData.columnCount+1) {
            row.add(resultSet.getString(i))
        }
        resultSetArray.add(row)
    }
    return resultSetArray
}
