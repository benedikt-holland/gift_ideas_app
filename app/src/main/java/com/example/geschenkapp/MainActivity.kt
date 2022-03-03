package com.example.geschenkapp

import CustomAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity() {
    lateinit var user: ResultSet
    lateinit var friendsFeed: ResultSet
    lateinit var giftFeed: ResultSet
    lateinit var adapter: CustomAdapter
    lateinit var rv: RecyclerView
    private lateinit var binding: ActivityMainBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
                      
        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                var inputStream = assets.open("config.properties")
                var props = Properties()
                props.load(inputStream)
                var usr = props.getProperty("MYSQL_USER", "")
                var pwd = props.getProperty("MYSQL_PWD", "")
                var url = props.getProperty("MYSQL_URL", "")
                inputStream.close()

                var db = DbConnector()
                db.connect(url, usr, pwd)
                user = db.loginUser("Hans@Müller.de", "password")
                user.next()
                val userId = user.getInt("id")
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
            } catch(e: FileNotFoundException) {
                System.err.println("Missing config.properties file in app/src/main/assets/ containing database credentials")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        //bottom navigation bar
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            Log.d("MainActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.ic_bottom_nav_notifications -> {
                    Log.d("NotificationActivity", "notification")
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Log.d("MainActivity", "item not found")
                }
            }
            true

        }
        rv = findViewById(R.id.recyclerview)
        rv.layoutManager = LinearLayoutManager(rv.context)
        rv.setHasFixedSize(true)

        binding.search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }

        })

        getListOfTest()
        /**
        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        // This loop will create 20 Views containing
        // the image with the count of view
        for (i in 1..20) {
            data.add(ItemsViewModel(R.drawable.ic_profile, "Item " + i))
        }

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter
        **/
    }
        

    private fun getListOfTest() {
        val testListAbc = ArrayList<String>()
        for (i in 1..20) {
            testListAbc.add("item $i")
        }
        adapter = CustomAdapter(testListAbc)
        rv.adapter = adapter
    }
}
