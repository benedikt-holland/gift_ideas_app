package com.example.geschenkapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.example.geschenkapp.databinding.ActivityLoginBinding
import com.example.geschenkapp.databinding.ActivityMainBinding




class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)





        getButtonClick()
    }

    private fun getButtonClick(){
        val btnSettings = findViewById(R.id.btnLogin) as Button
        btnSettings.setOnClickListener {



            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}