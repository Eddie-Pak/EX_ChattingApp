package com.example.chattingapp.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chattingapp.Key.Companion.DB_USERS
import com.example.chattingapp.LoginActivity
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentMypageBinding
import com.example.chattingapp.userlist.UserItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class MyPageFragment: Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)

        val currentUserId = Firebase.auth.currentUser?.uid ?: ""
        val myUserDB = Firebase.database.reference.child(DB_USERS).child(currentUserId)

        myUserDB.get().addOnSuccessListener {
           val currentUserItem = it.getValue(UserItem::class.java) ?: return@addOnSuccessListener

            binding.usernameEditText.setText(currentUserItem.username)
            binding.descriptionEditText.setText(currentUserItem.description)
        }

        binding.applyButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            if (username.isEmpty()) {
                Toast.makeText(context, "유저이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = mutableMapOf<String, Any>()
            user["username"] = username
            user["description"] = description
            myUserDB.updateChildren(user)
        }

        binding.logOutButton.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
        }
    }
}