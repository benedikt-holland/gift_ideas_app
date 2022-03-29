package com.example.geschenkapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.geschenkapp.fragments.EventsFragment
import com.example.geschenkapp.fragments.FriendslistFragment
import com.example.geschenkapp.fragments.GiftfeedFragment

private const val NUM_TABS = 3

//Adapter for tabview on profile page
class ProfileTabAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle,
                        val userId: Int, val friendUserId: Int
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        if (userId==friendUserId) { //Temporarly Removed
            when (position) {
                0 -> return GiftfeedFragment(userId, friendUserId, true)
                1 -> return EventsFragment()
            }
            return FriendslistFragment()
        } else {
            when (position) {
                0 -> return GiftfeedFragment(userId, friendUserId, false)
                1 -> return GiftfeedFragment(userId, friendUserId, true)
            }
            return EventsFragment()
        }
    }
}