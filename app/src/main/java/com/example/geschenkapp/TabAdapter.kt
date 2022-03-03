package com.example.geschenkapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.geschenkapp.fragments.EventsFragment
import com.example.geschenkapp.fragments.FriendslistFragment
import com.example.geschenkapp.fragments.WishlistFragment

private const val NUM_TABS = 3

class TabAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return WishlistFragment()
            1 -> return EventsFragment()
        }
        return FriendslistFragment()
    }
}