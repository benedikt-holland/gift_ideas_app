package com.example.geschenkapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import java.sql.ResultSet

//Class for the notifications activity
//Not implemented yet
class NotificationActivity : AppCompatActivity() {
    private lateinit var bottomNavBar: BottomNavigationView
    lateinit var user: ResultSet
    lateinit var db: DbConnector
    private lateinit var notificationsRv: RecyclerView
    private lateinit var notificationsAdapter: NotificationFeedAdapter
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = LoginHolder.getInstance().user
        db = DbHolder.getInstance().db
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_notification)
        //toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = getString(R.string.tab_notifications)
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        notificationsRv = findViewById(R.id.rvNotifications)
        notificationsRv.layoutManager = LinearLayoutManager(notificationsRv.context)
        notificationsRv.setHasFixedSize(true)


        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            loadNotifications(user.getInt("id"))
            setNotificationNumber()
        }

        useBottomNavBar()
    }

    override fun onResume() {
        super.onResume()
        useBottomNavBar()
        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            loadNotifications(user.getInt("id"))
            setNotificationNumber()
        }
    }

    //Load notifications of selected user and load into recyclerview
    private suspend fun loadNotifications(userId: Int) {
        val notificationsFeed = db.getNotificationFeed(userId)
        val notificationsArray = unloadResultSet(notificationsFeed)
        withContext(Dispatchers.Main) {
            if (notificationsRv.adapter==null) {
                notificationsAdapter = NotificationFeedAdapter(notificationsArray)
                notificationsRv.adapter = notificationsAdapter
            } else {
                notificationsAdapter.updateData(notificationsArray)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        menu.findItem(R.id.actionRemove).isVisible = true
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.actionShare -> {
                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.actionRemove -> {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {
                    db.removeAllNotifications(user.getInt("id"))
                }
                notificationsAdapter.updateData(ArrayList())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun useBottomNavBar(){
        bottomNavBar = findViewById(R.id.bottomNavigation)
        bottomNavBar.selectedItemId = R.id.ic_bottom_nav_notifications
        bottomNavBar.setOnItemSelectedListener { item ->
            Log.d("NotificationActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_profile -> {
                    Log.d("ProfileActivity", "notification")
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivityIfNeeded(intent, 0)
                }
                R.id.ic_bottom_nav_notifications -> {}
                else -> {
                    Log.d("NotificationActivity", "item not found")
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