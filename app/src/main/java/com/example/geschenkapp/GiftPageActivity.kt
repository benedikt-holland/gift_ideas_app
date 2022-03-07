package com.example.geschenkapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet

class GiftPageActivity  : AppCompatActivity() {

    lateinit var user: ResultSet
    lateinit var friendsFeed: ResultSet
    lateinit var giftPageCommentsAdapter: GiftpageCommentsAdapter
    lateinit var giftPageCommentsRv: RecyclerView
    private var db = DbConnector()
    private var userId: Int = -1


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
        /*giftPageCommentsRv = findViewById(R.id.rvGiftPageComments)
        giftPageCommentsRv.layoutManager = LinearLayoutManager(giftPageCommentsRv.context)
        giftPageCommentsRv.setHasFixedSize(true)*/

    }

    /*suspend fun loadFriendsFeed(userId: Int) {
        friendsFeed = db.getFriendsFeed(userId)
        val friendsFeedArray = unloadResultSet(friendsFeed)
        withContext(Dispatchers.Main) {
            giftPageCommentsAdapter = GiftpageCommentsAdapter(friendsFeedArray)
            giftPageCommentsRv.adapter = giftPageCommentsAdapter
            giftPageCommentsAdapter.notifyDataSetChanged()
        }
    }*/
}