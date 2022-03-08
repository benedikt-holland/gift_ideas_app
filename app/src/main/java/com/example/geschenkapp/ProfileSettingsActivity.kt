package com.example.geschenkapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapRegionDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geschenkapp.databinding.ActivityProfileSettingsBinding
import kotlinx.coroutines.*
import java.io.File
import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ProfileSettingsActivity : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var profilePicture: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Profile Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db



        binding.etFirstName.setText(user.getString("first_name"))
        binding.etLastName.setText(user.getString("last_name"))
        binding.etDateOfBirth.setText(user.getString("date_of_birth"))
        binding.etEmail.setText(user.getString("email"))

        val ivProfilepicture = binding.ivProfilepicture


        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            var inputStream = assets.open("config.properties")
            var props = Properties()
            props.load(inputStream)
            val profilePictureFileName = user.getString("profile_picture")

            var auth = props.getProperty("API_AUTH", "")
            var downloadUri = props.getProperty("API_DOWNLOAD", "") + profilePictureFileName
            inputStream.close()

            var imageConnector = ImageConnector()
            profilePicture = imageConnector.getImage(downloadUri, auth)
            withContext(Dispatchers.Main) {
                ivProfilepicture.setImageBitmap(profilePicture)
            }
        }



        spinnerProfilePrivacy()
        spinnerPostPrivacy()


        val btnSave = findViewById(R.id.btnSave) as Button
        btnSave.setOnClickListener {
//            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()

            //Read Data from frontend
            val firstName: String = binding.etFirstName.text.toString()
            val lastName: String = binding.etLastName.text.toString()
            val email: String = binding.etEmail.text.toString()
            val profilePrivacyString: String = binding.spProfilePrivacy.selectedItem.toString()
            val profilePrivacyArray: Array<String> =
                resources.getStringArray(R.array.profile_privacy_array)
            var profilePrivacy: Int = -1
            for (i in profilePrivacyArray.indices) {
                if (profilePrivacyArray[i].equals(profilePrivacyString)) {
                    profilePrivacy = i
                }
            }
            var dateOfBirthInput: String = binding.etDateOfBirth.text.toString()


            //Check if some values are empty
            if (firstName.equals("") || lastName.equals("") || email.equals("") || dateOfBirthInput.equals(
                    ""
                )
            ) {
                Toast.makeText(this, R.string.emptyString, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //Convert date input to date
            val dateOfBirth = LocalDate.parse(dateOfBirthInput, DateTimeFormatter.ISO_DATE)


            //Push new options to Database
            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {


                if (!db.checkIfEmailExistsOnOtherUser(user.getInt("id"), email)) {


                    var inputStream = assets.open("config.properties")
                    var props = Properties()
                    props.load(inputStream)

                    var auth = props.getProperty("API_AUTH", "")
                    var uploadUri = props.getProperty("API_UPLOAD", "")
                    inputStream.close()

                    var imageConnector = ImageConnector()
                    val response: String =
                        imageConnector.postImage(uploadUri, auth, profilePicture, user.getInt("id"))
//                    Looper.prepare()
//                    Toast.makeText(
//                        this@ProfileSettingsActivity,
//                        response.toString(),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Looper.loop()

                    val userId = user.getInt("id")

                    db.editUser(
                        userId, firstName, lastName,
                        dateOfBirth, email, profilePrivacy,
                        "$userId.png"
                    )
                } else {
                    Looper.prepare()
                    Toast.makeText(
                        this@ProfileSettingsActivity,
                        R.string.emailDuplicate,
                        Toast.LENGTH_SHORT
                    ).show()
                    Looper.loop()
                }


            }
        }
        val btnSelectImage = findViewById(R.id.btnSelectImage) as Button
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 3)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            var selectedImage = data.data!!
            profilePicture = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
//            binding.ivProfilepicture.setImageURI(selectedImage)
            binding.ivProfilepicture.setImageBitmap(profilePicture)
        }
    }

    private fun spinnerProfilePrivacy() {
        val spinner: Spinner = findViewById(R.id.spProfilePrivacy)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.profile_privacy_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }

    private fun spinnerPostPrivacy() {
        val spinner: Spinner = findViewById(R.id.spPostPrivacy)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.post_privacy_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }
}