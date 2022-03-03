package com.example.geschenkapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Connection
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream
import java.util.*

class ImageConnector : ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private lateinit var connection: Connection

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun getImage(imageUri : String, auth : String): Bitmap {
        var client: OkHttpClient = OkHttpClient().newBuilder().build()
        var request: Request = Request.Builder().url(imageUri).method("GET", null)
            .addHeader(
                "Authorization",
                auth
            )
            .build()
        var response: Response = client.newCall(request).execute()
        var byteStream = response.body?.byteStream()
        var bitmap : Bitmap = BitmapFactory.decodeStream(byteStream)
        return bitmap
    }
}