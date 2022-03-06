package com.example.geschenkapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.geschenkapp.databinding.ActivityProfileBinding
import kotlinx.coroutines.*
import java.util.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.FileNotFoundException
import java.lang.NullPointerException
import java.sql.ResultSet
import java.sql.SQLException

var initTabArray = arrayOf(
    "Geschenke",
    "Wunschliste",
    "Events",
    "Freunde"
)

class ProfileActivity : AppCompatActivity() {
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    private lateinit var binding: ActivityProfileBinding
    private lateinit var profilePicture: Bitmap
    lateinit var user: ResultSet
    lateinit var db: DbConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Profil"
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onResume() {
        super.onResume()

        //Get userId
        var friendUserId: Int = -1
        val b:Bundle? = intent.extras
        if (b!=null) {
            friendUserId = b.getInt("id")
        }
        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        //Show settings button for personal profile and add friend button for stranger profile
        var btnSettings: ImageButton = findViewById(R.id.btnSettings)
        var btnAddFriend: ImageButton = findViewById(R.id.btnAddFriend)
        var tabArray = initTabArray
        if (friendUserId==user.getInt("id")) {
            tabArray = initTabArray.slice(1..3).toTypedArray()
            btnSettings.visibility = View.VISIBLE
            btnAddFriend.visibility = View.GONE
        } else {
            tabArray = initTabArray.slice(0..2).toTypedArray()
            btnSettings.visibility = View.GONE
            btnAddFriend.visibility = View.VISIBLE
        }

        //Set tabs for profile page
        //Hide 'friends' tab for stranger profile and 'gifts' tab for personal profile
        tabLayout = findViewById(R.id.profileTabLayout)
        viewPager = findViewById(R.id.profileViewPager)
        val adapter = ProfileTabAdapter(supportFragmentManager, lifecycle, user.getInt("id"), friendUserId)
        viewPager.adapter = adapter
        binding = ActivityProfileBinding.inflate(layoutInflater)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabArray[position]
        }.attach()

        //Fill textviews with userdata
        var tvName: TextView = findViewById(R.id.tvName)
        var tvDateofbirth: TextView = findViewById(R.id.tvProfileDateofbirth)
        val ivProfilepicture: ImageView = findViewById(R.id.ivProfilepicture)

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {

            //Load user data
            var profileUser: ResultSet = db.getUser(user.getInt("id"), friendUserId)
            if (profileUser.next()) {
                withContext(Dispatchers.Main) {
                    tvName.text = if (profileUser.getString("last_name") != null) {
                        profileUser.getString("first_name") + " " + profileUser.getString("last_name")
                    } else {
                        profileUser.getString("first_name")
                    }
                    try {
                        tvDateofbirth.text = profileUser.getString("date_of_birth")
                    } catch(e: SQLException) {
                        tvDateofbirth.visibility = View.INVISIBLE
                        println("User privacy settings hides date of birth")
                    }
                }
            }

            //Load profile picture
            try {
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
            } catch (e: FileNotFoundException) {
                System.err.println("Missing config.properties file in app/src/main/assets/ containing database credentials")
            } catch (e: NullPointerException) {
                println("User has no profile picture")
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
        getButtonClick()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.actionShare -> {
                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getButtonClick(){
        val btnSettings = findViewById(R.id.btnSettings) as ImageButton
        btnSettings.setOnClickListener {
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
