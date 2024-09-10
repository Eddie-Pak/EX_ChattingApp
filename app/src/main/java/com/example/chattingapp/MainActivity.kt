package com.example.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.chattingapp.databinding.ActivityMainBinding
import com.example.chattingapp.userlist.UserFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val userFragment = UserFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = Firebase.auth.currentUser

        if (currentUser == null) {
            // 로그인이 안되어있음
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.userList -> {
                    replaceFragment(userFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.chatroomList -> {
                    return@setOnItemSelectedListener true
                }
                R.id.mayPage -> {
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.frameLayout, fragment)
                commit()
            }
    }
}