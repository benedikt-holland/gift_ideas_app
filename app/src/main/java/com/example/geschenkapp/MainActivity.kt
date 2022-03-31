package com.example.geschenkapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
    lateinit var friendsFeedAdapter: FriendsFeedAdapter
    private lateinit var rvFriendsFeed: RecyclerView
    private var db = DbConnector()
    private var userId: Int = -1
    private lateinit var binding: ActivityMainBinding
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Check internet connection
        if (!checkForInternet(this)) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        //Get user data and database connector
        user = LoginHolder.getInstance().user
        db = DbHolder.getInstance().db

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
        rvFriendsFeed = findViewById(R.id.rvFriendsFeed)
        rvFriendsFeed.layoutManager = LinearLayoutManager(rvFriendsFeed.context)
        rvFriendsFeed.setHasFixedSize(true)

        //Listener for clicking on recyclerview cards. Opens profile page of selected user
        binding.svHome.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
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
                                        startActivity(intent)
                                        intent.removeExtra("id")
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.search_error, query),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                    } catch (e: Exception) {}
                return true
                }
                return false
            }

            //Listen for searchbar changes
            override fun onQueryTextChange(newText: String?): Boolean {
                friendsFeedAdapter.filter.filter(newText)
                return false
            }
        })
    }

    //Will be called when returning from other activity
    override fun onResume() {
        super.onResume()
        db = DbHolder.getInstance().db

        //Refresh friendsfeed
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
        binding.mBottomNavigation.selectedItemId = R.id.ic_bottom_nav_home
        binding.mBottomNavigation.setOnItemSelectedListener { item ->
            Log.d("MainActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_profile -> {
                    //Launch profile activity and pass id of logged user
                    val intent = Intent(this, ProfileActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", userId)
                    intent.putExtras(b)
                    startActivity(intent)
                    intent.removeExtra("id")
                }
                R.id.ic_bottom_nav_notifications -> {
                    //Launch notifications activity
                    Log.d("NotificationActivity", "notification")
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
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

    @SuppressLint("NotifyDataSetChanged")
    suspend fun loadFriendsFeed(userId: Int) {
        friendsFeed = db.getFriendsFeed(userId)
        val friendsFeedArray = unloadResultSet(friendsFeed)
        withContext(Dispatchers.Main) {
            friendsFeedAdapter = FriendsFeedAdapter(friendsFeedArray)
            rvFriendsFeed.adapter = friendsFeedAdapter
            friendsFeedAdapter.notifyDataSetChanged()
        }
    }

    private suspend fun setNotificationNumber(){
        val count = db.getNotificationCount(userId)
        withContext(Dispatchers.Main) {
            binding.mBottomNavigation.getOrCreateBadge(R.id.ic_bottom_nav_notifications).apply {
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

//Helper function to check internet connection
fun checkForInternet(context: Context): Boolean {
    // register activity with the connectivity manager service
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}
