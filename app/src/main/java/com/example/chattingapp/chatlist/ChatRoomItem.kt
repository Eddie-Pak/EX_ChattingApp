package com.example.chattingapp.chatlist

data class ChatRoomItem(
    val chatRoomId: String,
    val otherUserName: String,
    val lastMessage: String,
)