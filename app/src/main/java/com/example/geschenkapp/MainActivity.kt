package com.example.geschenkapp

import CustomAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var adapter: CustomAdapter
    lateinit var rv: RecyclerView
    private lateinit var binding: ActivityMainBinding

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.actionBack -> {
                Toast.makeText(this, "back", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.actionShare -> {
                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
