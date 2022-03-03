package com.example.geschenkapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Connection
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream

class ImageConnector : ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private lateinit var connection: Connection

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun getImage(imageUri : String): Bitmap {
        var client: OkHttpClient = OkHttpClient().newBuilder().build()
        var request: Request = Request.Builder().url(imageUri).method("GET", null)
            .addHeader(
                "Authorization",
                "Basic YW5kcm9pZDpxcnhnLVArV1I8eS1YcFM/SGBtXUknSmxGfihbY0xxPnBBWERLPn4oR05GeiVwNFNxOCx3bmRdVXI0Lj5rT3wm"
            )
            .build()
        var response: Response = client.newCall(request).execute()
        var byteStream = response.body?.byteStream()
        var bitmap : Bitmap = BitmapFactory.decodeStream(byteStream)
        return bitmap
    }
}