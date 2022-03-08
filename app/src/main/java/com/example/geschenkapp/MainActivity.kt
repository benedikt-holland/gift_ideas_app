package com.example.geschenkapp

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.sql.ResultSet
import java.util.*
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var user: ResultSet
    lateinit var friendsFeed: ResultSet
    lateinit var giftFeed: ResultSet
    lateinit var friendsFeedAdapter: FriendsFeedAdapter
    lateinit var friendsFeedRv: RecyclerView
    private var db = DbConnector()
    private var userId: Int = -1
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                userId = user.getInt("id")
                loadFriendsFeed(userId)
                try {
                    friendsFeed = db.getFriendsFeed(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    giftFeed = db.getGiftFeedByMemberId(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }  catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar()

        //bottom navigation bar
        binding.bottomNavigation.selectedItemId = R.id.ic_bottom_nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            Log.d("MainActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_profile -> {
                    var intent = Intent(this, ProfileActivity::class.java)
                    var b = Bundle()
                    b.putInt("id", userId)
                    intent.putExtras(b)
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_notifications -> {
                    Log.d("NotificationActivity", "notification")
                    val intent = Intent(this, NotificationActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_home -> {
                    true
                }
                else -> {
                    Log.d("MainActivity", "item not found")
                }
            }
            true

        }

        setNotificationNumber()

        friendsFeedRv = findViewById(R.id.rvFriendsFeed)
        friendsFeedRv.layoutManager = LinearLayoutManager(friendsFeedRv.context)
        friendsFeedRv.setHasFixedSize(true)

        binding.search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query!=null) {
                    try {
                        val viewModelJob = SupervisorJob()
                        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                        uiScope.launch(Dispatchers.IO) {
                                val profile: ResultSet = db.searchUser(userId, query)
                                withContext(Dispatchers.Main) {
                                    try {
                                        profile.next()
                                        var intent =
                                            Intent(this@MainActivity, ProfileActivity::class.java)
                                        var b = Bundle()
                                        b.putInt("id", profile.getInt("id"))
                                        intent.putExtras(b)
                                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
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

            override fun onQueryTextChange(newText: String?): Boolean {
                friendsFeedAdapter.filter.filter(newText)
                return false
            }

        })

        getButtonClick()

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
        

    suspend fun loadGiftFeed(userId: Int) {
        val giftList = ArrayList<ArrayList<String>>()
        giftFeed = db.getGiftFeedByMemberId(userId)
        while(giftFeed.next()) {
            var row = ArrayList<String>()
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

    suspend fun loadFriendsFeed(userId: Int) {
        friendsFeed = db.getFriendsFeed(userId)
        val friendsFeedArray = unloadResultSet(friendsFeed)
        withContext(Dispatchers.Main) {
            friendsFeedAdapter = FriendsFeedAdapter(friendsFeedArray)
            friendsFeedRv.adapter = friendsFeedAdapter
            friendsFeedAdapter.notifyDataSetChanged()
        }
    }

    private fun getButtonClick(){
        val btnStar = findViewById(R.id.btnFavorites) as Button
        btnStar.setOnClickListener {
            Toast.makeText(this, "favorites", Toast.LENGTH_SHORT).show()
        }
        val btnGift = findViewById(R.id.btnGeschenk) as Button
        btnGift.setOnClickListener {
            Toast.makeText(this, "gift", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setNotificationNumber(){
        binding.bottomNavigation.getOrCreateBadge(R.id.ic_bottom_nav_notifications).apply {
            number = 10
            isVisible = true
        }
    }

    private fun setSupportActionBar(){
        //toolbar
        supportActionBar?.apply {
            title = "Home"
        }
    }
}

fun unloadResultSet(resultSet: ResultSet): ArrayList<ArrayList<String>> {
    var resultSetArray = ArrayList<ArrayList<String>>()
    while(resultSet.next()) {
        var row = ArrayList<String>()
        for (i in 1 until resultSet.metaData.columnCount+1) {
            row.add(resultSet.getString(i))
        }
        resultSetArray.add(row)
    }
    return resultSetArray
}
