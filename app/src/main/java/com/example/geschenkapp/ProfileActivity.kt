package com.example.geschenkapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.geschenkapp.databinding.ActivityProfileBinding
import kotlinx.coroutines.*
import java.util.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.geschenkapp.exceptions.NoUserException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.FileNotFoundException
import java.sql.ResultSet

val tabArray = arrayOf(
    "Wunschliste",
    "Events",
    "Freunde"
)

class ProfileActivity : AppCompatActivity() {
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var bottomNavBar: BottomNavigationView
    private lateinit var binding: ActivityProfileBinding
    private lateinit var profilePicture: Bitmap
    lateinit var user: ResultSet

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

        useBottomNavBar()

        tabLayout = findViewById(R.id.profileTabLayout)
        viewPager = findViewById(R.id.profileViewPager)

        val adapter = TabAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabArray[position]
        }.attach()

        binding = ActivityProfileBinding.inflate(layoutInflater)

        val ivProfilepicture: ImageView = findViewById(R.id.ivProfilepicture)

        var userId : Int = intent.extras?.getInt("userId") ?: throw NoUserException("There are no extras to read the userId from!")
        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                var inputStream = assets.open("config.properties")
                var props = Properties()
                props.load(inputStream)
                var usr = props.getProperty("MYSQL_USER", "")
                var pwd = props.getProperty("MYSQL_PWD", "")
                var url = props.getProperty("MYSQL_URL", "")


                var db = DbConnector()
                db.connect(url, usr, pwd)
                user = db.getUserById(userId)
                user.next()
                val profilePictureFileName = user.getString("profile_picture")

                var auth = props.getProperty("API_AUTH", "")
                var downloadUri = props.getProperty("API_DOWNLOAD", "") + profilePictureFileName
                inputStream.close()

                var imageConnector = ImageConnector()
                profilePicture = imageConnector.getImage(downloadUri, auth)
                withContext(Dispatchers.Main) {
                    ivProfilepicture.setImageBitmap(profilePicture)
                }

            } catch(e: FileNotFoundException) {
                System.err.println("Missing config.properties file in app/src/main/assets/ containing database credentials")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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


    /*override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        super.onBackPressed()
    }*/

    private fun useBottomNavBar(){
        bottomNavBar = findViewById(R.id.bottomNavigation)
        bottomNavBar.selectedItemId = R.id.ic_bottom_nav_profile
        bottomNavBar.setOnItemSelectedListener { item ->
            Log.d("ProfileActivity", "item clicked")
            when (item.itemId) {
                R.id.ic_bottom_nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    //finish()
                    //finishAffinity()
                }
                R.id.ic_bottom_nav_notifications -> {
                    Log.d("NotificationActivity", "notification")
                    val intent = Intent(this, NotificationActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    //finish()
                }
                R.id.ic_bottom_nav_profile -> {
                    true
                }
                else -> {
                    Log.d("ProfileActivity", "item not found")
                }
            }
            true

        }
    }
}
