package com.example.geschenkapp

import CustomAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    class MainActivity : AppCompatActivity() {
        private lateinit var linearLayoutManager: LinearLayoutManager
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
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


            initProfileViewPager()
        }

        private fun initProfileViewPager() {
            var viewPager : ViewPager2 = findViewById(R.id.profileViewPager)
            //var adapter = ViewPAger
        }
    }
}