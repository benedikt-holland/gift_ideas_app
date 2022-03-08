package com.example.geschenkapp

import android.app.ActionBar
import android.app.DatePickerDialog
import android.app.UiModeManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent
import android.graphics.Bitmap
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.example.geschenkapp.databinding.ActivityProfileSettingsBinding
import kotlinx.coroutines.*
import java.io.File
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class ProfileSettingsActivity : AppCompatActivity() {

    lateinit var uiModeManager: UiModeManager
    lateinit var user: ResultSet
    lateinit var db: DbConnector
    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var profilePicture: Bitmap
    var calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = getResources().getString(R.string.title_activity_profile_settings)
            setHomeButtonEnabled(true)
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
        binding.tvDateOfBirth.setText(user.getString("date_of_birth"))
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

        getButtonClick()

        setDate()
    }

    private fun getButtonClick(){

        val btnSelectImage = findViewById(R.id.btnSelectImage) as Button
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 3)
        }
        val btnSave = findViewById(R.id.btnSave) as Button
        btnSave.setOnClickListener {

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
            var dateOfBirthInput: String = binding.tvDateOfBirth.text.toString()


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
            getSwitchState()


            //Push new options to Database
            val viewModelJob2 = SupervisorJob()
            val uiScope2 = CoroutineScope(Dispatchers.Main + viewModelJob2)
            uiScope2.launch(Dispatchers.IO) {


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

                    val userId = user.getInt("id")

                    var tempUser = db.editUser(
                        userId, firstName, lastName,
                        dateOfBirth, email, profilePrivacy,
                        "$userId.png"
                    )

                    tempUser.next()
                    user = tempUser
                    DataHolder.getInstance().user = user

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

            binding.etFirstName.setText(user.getString("first_name"))
            binding.etLastName.setText(user.getString("last_name"))
            binding.tvDateOfBirth.setText(user.getString("date_of_birth"))
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

            getButtonClick()

            setDate()
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

    private fun getSwitchState() {
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.swDarkMode.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                // when switch button is checked
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            } else {
                // if switch button is unchecked
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }
        }

    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
    }

    private fun setDate(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var mt = "0"
        var d = "0"


        binding.tvDateOfBirth.setOnClickListener{
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener{view, mYear, mMonth, mDay ->
                val month2 = mMonth + 1
                if(month2<10){
                    mt = "0" + month2
                } else{
                    mt = month2.toString()
                }
                if(mDay<10){
                    d = "0" + mDay
                } else{
                    d = mDay.toString()
                }
                binding.tvDateOfBirth.setText(""+ mYear + "-" + mt + "-"+ d )
            }, year, month, day)
            dpd.show()
        }
    }
}
