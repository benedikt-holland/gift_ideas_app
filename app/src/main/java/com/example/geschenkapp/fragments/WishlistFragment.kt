package com.example.geschenkapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.geschenkapp.GiftPageActivity
import com.example.geschenkapp.R

//Fragment Container for Tab Wishlist on Profile page
class WishlistFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.whishlist, container, false)
        var giftpageButton = view.findViewById(R.id.btnGiftpage) as Button
        giftpageButton.setOnClickListener {
            val intent = Intent(activity, GiftPageActivity::class.java)
            startActivity(intent)
        }
        return view
    }
}
