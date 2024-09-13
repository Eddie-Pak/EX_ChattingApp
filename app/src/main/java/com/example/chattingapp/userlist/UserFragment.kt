package com.example.chattingapp.userlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.Key.Companion.DB_CHAT_ROOMS
import com.example.chattingapp.Key.Companion.DB_USERS
import com.example.chattingapp.R
import com.example.chattingapp.chatdetail.ChatActivity
import com.example.chattingapp.chatdetail.ChatActivity.Companion.EXTRA_CHAT_ROOM_ID
import com.example.chattingapp.chatdetail.ChatActivity.Companion.EXTRA_OTHER_USER_ID
import com.example.chattingapp.chatlist.ChatRoomItem
import com.example.chattingapp.databinding.FragmentUserlistBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.UUID

class UserFragment: Fragment(R.layout.fragment_userlist) {

    private lateinit var binding: FragmentUserlistBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserlistBinding.bind(view)

        val userListAdapter = UserAdapter{ otherUSer ->
            val myUserId = Firebase.auth.currentUser?.uid ?: ""
            val chatRoomDB = Firebase.database.reference.child(DB_CHAT_ROOMS).child(myUserId).child(otherUSer.userId ?: "")

            chatRoomDB.get().addOnSuccessListener {
                var chatRoomId = ""
                if (it.value != null) {
                    val chatRoom = it.getValue(ChatRoomItem::class.java)
                    chatRoomId = chatRoom?.chatRoomId ?: ""
                } else {
                    chatRoomId = UUID.randomUUID().toString()

                    val newChatRoom = ChatRoomItem(
                        chatRoomId = chatRoomId,
                        otherUserName = otherUSer.username,
                        otherUserId = otherUSer.userId,
                    )
                    chatRoomDB.setValue(newChatRoom)
                }

                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(EXTRA_OTHER_USER_ID, otherUSer.userId)
                intent.putExtra(EXTRA_CHAT_ROOM_ID, chatRoomId)

                startActivity(intent)
            }
        }
        binding.userListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userListAdapter
        }

        val currentUserId = Firebase.auth.currentUser?.uid ?: ""

        Firebase.database.reference.child(DB_USERS)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val userItemList = mutableListOf<UserItem>()

                    snapshot.children.forEach {
                        val user = it.getValue(UserItem::class.java) ?: return

                        if (user.userId != currentUserId) {
                            userItemList.add(user)
                        }
                    }

                    userListAdapter.submitList(userItemList)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}