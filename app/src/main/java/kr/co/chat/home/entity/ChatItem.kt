package kr.co.chat.home.entity

data class ChatItem(
    val senderId : String,
    val senderEmail : String,
    val message : String
) {
    constructor() : this("" , "" , "")
}

