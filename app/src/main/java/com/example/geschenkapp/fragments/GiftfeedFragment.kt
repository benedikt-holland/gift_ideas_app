package com.example.geschenkapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.*
import kotlinx.coroutines.*

//Fragment Container for Tab Giftideas on Profile page
class GiftfeedFragment(userId: Int, friendUserId: Int, isWish: Boolean = true): Fragment() {
    lateinit var profileFeedRv: RecyclerView
    lateinit var profileFeedAdapter: ProfileFeedAdapter
    lateinit var db: DbConnector
    val isWish: Boolean = isWish
    val userId: Int = userId
    val friendUserId: Int = friendUserId
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.giftfeed, container, false)
        db = DbHolder.getInstance().db
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileFeedRv = view.findViewById(R.id.rvGiftFeed)
        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            loadGiftFeed(userId, friendUserId)
        }
        profileFeedRv.layoutManager = LinearLayoutManager(profileFeedRv.context)
        profileFeedRv.setHasFixedSize(true)
    }

    suspend fun loadGiftFeed(userId: Int, friendUserId: Int ) {
        val giftFeed = db.getGiftFeedByUserId(userId, friendUserId)
        val giftFeedArray = unloadResultSet(giftFeed)
        withContext(Dispatchers.Main) {
            profileFeedAdapter = ProfileFeedAdapter(giftFeedArray)
            profileFeedRv.adapter = profileFeedAdapter
            profileFeedAdapter.notifyDataSetChanged()
        }
    }

}