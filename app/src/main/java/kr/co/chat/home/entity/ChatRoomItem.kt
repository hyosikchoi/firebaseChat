package kr.co.chat.home.entity

data class ChatRoomItem(
    val key : String,
    val title : String,
    val buyerId : String,
    val sellerId : String,
    val buyerEmail : String,
    val sellerEmail : String
) {
    constructor() : this("" , "" , "" , "" , "" , "")
}
