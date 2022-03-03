package com.example.geschenkapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.geschenkapp.databinding.ActivityProfileBinding
import kotlinx.coroutines.*
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var profilePicture: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        binding = ActivityProfileBinding.inflate(layoutInflater)


        val ivProfilepicture : ImageView = findViewById(R.id.ivProfilepicture)

        val viewModelJob = SupervisorJob()
        var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO){
            var inputStream = assets.open("config.properties")
            var props = Properties()
            props.load(inputStream)
            var auth = props.getProperty("API_AUTH", "")
            var downloadUri = props.getProperty("API_DOWNLOAD", "") + "bild.png"
            inputStream.close()

            var imageConnector = ImageConnector()
            profilePicture = imageConnector.getImage(downloadUri, auth)
            withContext(Dispatchers.Main) {
                ivProfilepicture.setImageBitmap(profilePicture)
            }

        }
    }
}