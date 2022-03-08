package com.example.geschenkapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
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

    fun postImage(postUrl : String, auth : String, profilePicture: Bitmap, userId: Int):String{
        var stream = ByteArrayOutputStream()
        profilePicture.compress(Bitmap.CompressFormat.PNG, 100, stream)
        var byteArray = stream.toByteArray()



        var requestBody : RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "$userId.png",
                RequestBody.create("image/png".toMediaType(), byteArray)
            ).build()

        var client: OkHttpClient = OkHttpClient().newBuilder().build()
        var request: Request = Request.Builder().url(postUrl).method("POST", requestBody)
            .addHeader("Authorization", auth).build()
        var response = client.newCall(request).execute()
        return response.body.toString()

    }
}