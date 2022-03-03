package com.example.geschenkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var user: ResultSet
    lateinit var friendsFeed: ResultSet
    lateinit var giftFeed: ResultSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initProfileViewPager()


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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initProfileViewPager() {
        //var viewPager : ViewPager2 = findViewById(R.id.profileViewPager)
        //var adapter = ViewPAger
    }
}