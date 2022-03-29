package com.example.geschenkapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.UiModeManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Looper
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.example.geschenkapp.databinding.ActivityProfileSettingsBinding
import kotlinx.coroutines.*
import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


//Class for profile settings page
@Suppress("DEPRECATION")
class ProfileSettingsActivity : AppCompatActivity() {

    lateinit var uiModeManager: UiModeManager
    lateinit var user: ResultSet
    lateinit var db: DbConnector
    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var profilePicture: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = resources.getString(R.string.title_activity_profile_settings)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        user = LoginHolder.getInstance().user


        db = DbHolder.getInstance().db



        binding.etFirstName.setText(user.getString("first_name"))
        binding.etLastName.setText(user.getString("last_name"))
        binding.tvDateOfBirth.text = user.getString("date_of_birth")
        binding.etEmail.setText(user.getString("email"))

        val ivProfilepicture = binding.ivProfilepicture


        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            val inputStream = assets.open("config.properties")
            val props = Properties()
            props.load(inputStream)
            val profilePictureFileName = user.getString("profile_picture")

            val auth = props.getProperty("API_AUTH", "")
            val downloadUri = props.getProperty("API_DOWNLOAD", "") + profilePictureFileName
            inputStream.close()

            val imageConnector = ImageConnector()
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
        val tvDeleteAccount = findViewById<TextView>(R.id.tvDeleteAccount)
        tvDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this@ProfileSettingsActivity)
            builder.setMessage(R.string.delete_account_dialog)
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { _, _ ->
                    // Delete selected note from database
                    val alert = builder.create()
                    alert.show()
                    val pref = getSharedPreferences("com.example.geschenkapp", MODE_PRIVATE)
                    pref.edit().remove("email").apply()
                    pref.edit().remove("password").apply()

                    val viewModelJob = SupervisorJob()
                    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                    uiScope.launch(Dispatchers.IO) {
                        db.deleteAccount(user.getInt("id"))
                    }

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }


        val tvLogout = findViewById<TextView>(R.id.tvLogout)
        tvLogout.setOnClickListener {
            val pref = getSharedPreferences("com.example.geschenkapp", MODE_PRIVATE)
            pref.edit().remove("email").apply()
            pref.edit().remove("password").apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val ivProfilepicture = findViewById<ImageView>(R.id.ivProfilepicture)
        ivProfilepicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 3)
        }

        val tvChangeProfilepicture = findViewById<TextView>(R.id.tvChangeProfilepicture)
        tvChangeProfilepicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 3)
        }

        val btnSave = findViewById<Button>(R.id.btnSave)
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
                if (profilePrivacyArray[i] == profilePrivacyString) {
                    profilePrivacy = i
                }
            }
            val dateOfBirthInput: String = binding.tvDateOfBirth.text.toString()


            //Check if some values are empty
            if (firstName == "" || lastName == "" || email == "" || dateOfBirthInput == ""
            ) {
                Toast.makeText(this, R.string.empty_string, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //Convert date input to date
            val dateOfBirth = LocalDate.parse(dateOfBirthInput, DateTimeFormatter.ISO_DATE)


            //Push new options to Database
            val viewModelJob2 = SupervisorJob()
            val uiScope2 = CoroutineScope(Dispatchers.Main + viewModelJob2)
            uiScope2.launch(Dispatchers.IO) {


                if (!db.checkIfEmailExistsOnOtherUser(user.getInt("id"), email)) {


                    val inputStream = assets.open("config.properties")
                    val props = Properties()
                    props.load(inputStream)

                    val auth = props.getProperty("API_AUTH", "")
                    val uploadUri = props.getProperty("API_UPLOAD", "")
                    inputStream.close()

                    val imageConnector = ImageConnector()
                    imageConnector.postImage(uploadUri, auth, profilePicture, user.getInt("id"))

                    val userId = user.getInt("id")

                    val tempUser = db.editUser(
                        userId, firstName, lastName,
                        dateOfBirth, email, profilePrivacy,
                        "$userId.png"
                    )

                    tempUser.next()
                    user = tempUser
                    LoginHolder.getInstance().user = user

                } else {
                    Looper.prepare()
                    Toast.makeText(
                        this@ProfileSettingsActivity,
                        R.string.email_duplicate,
                        Toast.LENGTH_SHORT
                    ).show()
                    Looper.loop()
                    return@launch
                }
                Looper.prepare()
                Toast.makeText(this@ProfileSettingsActivity, R.string.successful_save, Toast.LENGTH_SHORT).show()
                Looper.loop()

            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data!!
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

    @SuppressLint("SetTextI18n")
    private fun setDate(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var mt: String
        var d: String

        binding.tvDateOfBirth.setOnClickListener{
            val dpd = DatePickerDialog(this, { _, mYear, mMonth, mDay ->
                val month2 = mMonth + 1
                mt = if(month2<10){
                    "0$month2"
                } else{
                    month2.toString()
                }
                d = if(mDay<10){
                    "0$mDay"
                } else{
                    mDay.toString()
                }
                binding.tvDateOfBirth.text = "$mYear-$mt-$d"
            }, year, month, day)
            dpd.show()
        }
    }
}
