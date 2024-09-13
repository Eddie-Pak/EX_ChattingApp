package com.example.chattingapp.chatdetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.Key
import com.example.chattingapp.Key.Companion.DB_CHATS
import com.example.chattingapp.Key.Companion.DB_CHAT_ROOMS
import com.example.chattingapp.Key.Companion.DB_USERS
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ActivityChatBinding
import com.example.chattingapp.userlist.UserItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val chatAdapter = ChatAdapter()

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        myUserId = Firebase.auth.currentUser?.uid ?: ""

        Firebase.database.reference.child(DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""
            }

        Firebase.database.reference.child(DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)

                chatAdapter.otherUserItem = otherUserItem
            }

        Firebase.database.reference.child(DB_CHATS).child(chatRoomId).addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatItem = snapshot.getValue(ChatItem::class.java)
                chatItem ?: return

                chatItemList.add(chatItem)
                chatAdapter.submitList(chatItemList)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildRemoved(snapshot: DataSnapshot) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onCancelled(error: DatabaseError) { }
        })

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "메세지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId,
            )

            Firebase.database.reference.child(DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
                "${DB_CHAT_ROOMS}/$myUserId/$otherUserId/lastMessage" to message,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/lastMessage" to message,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserName" to myUserName,
            )
            Firebase.database.reference.updateChildren(updates)

            binding.messageEditText.text.clear()
        }
    }

    companion object {
        const val EXTRA_CHAT_ROOM_ID = "CHAT_ROOM_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
    }
}