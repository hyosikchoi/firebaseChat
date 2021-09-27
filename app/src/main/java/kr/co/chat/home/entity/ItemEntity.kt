package kr.co.chat.home.entity

data class ItemEntity(
    val sellerId : String,
    val imageUrl : String,
    val price : String,
    val title : String,
    val createdAt : Long
)
