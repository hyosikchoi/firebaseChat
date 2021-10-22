package kr.co.chat.home.entity

import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng

data class ItemEntity(
    val id : String,
    val sellerId : String,
    val sellerEmail : String,
    val imageUrl : String,
    val price : String,
    val title : String,
    val createdAt : Long,
    val latitude : Double,
    val longitude : Double
) : TedClusterItem {
    constructor() : this("", "" , "" ,"" , "" , "" , 0 , 0.0 , 0.0)

    override fun getTedLatLng(): TedLatLng {
        return TedLatLng(latitude = latitude , longitude = longitude)
    }
}
