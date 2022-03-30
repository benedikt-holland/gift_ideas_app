package com.example.geschenkapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.SupervisorJob
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

//Connector for setting profile picture
class ImageConnector : ViewModel() {
    private val viewModelJob = SupervisorJob()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun getImage(imageUri : String, auth : String): Bitmap {
        val client: OkHttpClient = OkHttpClient().newBuilder().build()
        val request: Request = Request.Builder().url(imageUri).method("GET", null)
            .addHeader(
                "Authorization",
                auth
            )
            .build()
        val response: Response = client.newCall(request).execute()
        val byteStream = response.body?.byteStream()
        val bitmap : Bitmap = BitmapFactory.decodeStream(byteStream)
        response.close()
        return bitmap
    }

    fun postImage(postUrl : String, auth : String, profilePicture: Bitmap, userId: Int):String{
        val stream = ByteArrayOutputStream()
        profilePicture.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()



        val requestBody : RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "$userId.png",
                byteArray.toRequestBody("image/png".toMediaType(), 0, byteArray.size)
            ).build()

        val client: OkHttpClient = OkHttpClient().newBuilder().build()
        val request: Request = Request.Builder().url(postUrl).method("POST", requestBody)
            .addHeader("Authorization", auth).build()
        val response = client.newCall(request).execute()
        val string = response.body.toString()
        response.close()
        return string

    }
}