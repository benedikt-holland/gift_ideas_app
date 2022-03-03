package com.example.geschenkapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.geschenkapp.data.User
import com.example.geschenkapp.databinding.ActivityProfileBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.*
import okhttp3.internal.http2.Header
import java.io.File
import java.io.InputStream
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var profilePicture: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        binding = ActivityProfileBinding.inflate(layoutInflater)


        val imageUri = "http://montesvoss.de:8080/downloadFile/bild.png"
        val ivProfilepicture : ImageView = findViewById(R.id.ivProfilepicture)


        val viewModelJob = SupervisorJob()
        var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO){
            var imageConnector = ImageConnector()
            profilePicture = imageConnector.getImage(imageUri)
            withContext(Dispatchers.Main) {
                ivProfilepicture.setImageBitmap(profilePicture)
            }

        }
    }
}