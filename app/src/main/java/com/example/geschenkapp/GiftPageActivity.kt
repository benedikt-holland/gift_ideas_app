package com.example.geschenkapp

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.geschenkapp.databinding.ActivityProfileBinding
import com.google.android.material.tabs.TabLayout

class GiftPageActivity  : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giftpage)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Für: Johannes"
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
}