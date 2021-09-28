package kr.co.chat.home.entity

data class ItemEntity(
    val sellerId : String,
    val imageUrl : String,
    val price : String,
    val title : String,
    val createdAt : Long,
    val latitude : Double,
    val longitude : Double
) {
    constructor() : this("" , "" , "" , "" , 0 , 0.0 , 0.0)
}
