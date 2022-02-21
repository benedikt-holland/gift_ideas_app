package com.example.geschenkapp

import CustomAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    class MainActivity : AppCompatActivity() {
        lateinit var adapter: CustomAdapter
        lateinit var rv: RecyclerView
        private lateinit var binding: ActivityMainBinding

        private lateinit var linearLayoutManager: LinearLayoutManager
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //setContentView(R.layout.activity_main)
            binding = ActivityMainBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)

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

            initProfileViewPager()
        }

        private fun initProfileViewPager() {
            var viewPager : ViewPager2 = findViewById(R.id.profileViewPager)
            //var adapter = ViewPAger
        }

        private fun getListOfTest() {
            val isoCountryCodes = Locale.getISOCountries()
            val countryListWithEmojis = ArrayList<String>()
            for (countryCode in isoCountryCodes) {
                val locale = Locale("", countryCode)
                val countryName = locale.displayCountry
                val flagOffset = 0x1F1E6
                val asciiOffset = 0x41
                val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
                val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset
                val flag =
                    (String(Character.toChars(firstChar)) + String(Character.toChars(secondChar)))
                countryListWithEmojis.add("$countryName $flag")
            }
            adapter = CustomAdapter(countryListWithEmojis)
            rv.adapter = adapter
        }


    private fun initProfileViewPager() {
        //var viewPager : ViewPager2 = findViewById(R.id.profileViewPager)
        //var adapter = ViewPAger
    }
}
