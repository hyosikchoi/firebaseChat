package kr.co.chat.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kr.co.chat.DBKey
import kr.co.chat.R
import kr.co.chat.databinding.FragmentHomeBinding

import kr.co.chat.extension.toast
import kr.co.chat.home.adapter.ViewPagerAdapter
import kr.co.chat.home.detail.ItemInsertActivity
import kr.co.chat.home.entity.ChatRoomItem
import kr.co.chat.home.entity.ItemEntity
import ted.gun0912.clustering.naver.TedNaverClustering
import ted.gun0912.clustering.naver.TedNaverMarker

class HomeFragment : Fragment(R.layout.fragment_home)  , OnMapReadyCallback {

    private var _binding : FragmentHomeBinding ?= null

    private val binding get() = _binding!!

    private lateinit var naverMap : NaverMap

    private lateinit var locationSource: FusedLocationSource

    private lateinit var naverClustering: TedNaverClustering<ItemEntity>

    /** 카메라 위치 저장하기 위한 preference */
    private var sharedPreferences: SharedPreferences ?= null

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val userDB : DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.USERS)
    }

    private val itemsDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.ITEMS)
    }

    private val itemList = mutableListOf<ItemEntity>()

    private val viewPagerAdapter = ViewPagerAdapter(
        /** viewPager item 클릭 시 채팅방 개설 */
        itemClicked = { itemEntity ->
            if(auth.currentUser == null) {
                context?.let {
                    it.toast("로그인 후 이용해주세요.")
                }
            }
            else {
                context?.let {
                    AlertDialog.Builder(it)
                        .setTitle("채팅방을 개설 하시겠습니까?")
                        .setPositiveButton("확인") { _, _ ->
                            createChatRoom(itemEntity)
                        }
                        .setNegativeButton("취소") { _, _ -> }
                        .create()
                        .show()
                }
            }
        }
    )

    /** fragment 전환 시 생명주기 때문에 listener 를 최상단에 정의 */
    /** lifecycle 에 맞춰 add 했다가 remove 하기 위해서 */
    // TODO 마커가 물품 등록 후 바로 업데이트 되게끔 수정
    // TODO 가끔 마커가 중복으로 등록되는 경우 수정
    private val itemListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          //  clearMarker()
            itemList.clear()

            snapshot.children.forEach { data ->
                val itemEntity = data.getValue(ItemEntity::class.java)
                itemEntity ?: return
                itemList.add(itemEntity)
            }
                viewPagerAdapter.submitList(itemList) {
//                    context?.toast(viewPagerAdapter.currentList.size.toString())
                    updateMarker(itemList = itemList)
                }
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync(this)

        binding.itemViewPager.adapter = viewPagerAdapter

        binding.itemViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                /** 해당 item 가져온다. */
                val itemEntity = viewPagerAdapter.currentList[position]

                val cameraUpdate =  CameraUpdate.scrollTo(LatLng(itemEntity.latitude,itemEntity.longitude)).animate(CameraAnimation.Easing)
                if(this@HomeFragment::naverMap.isInitialized) naverMap.moveCamera(cameraUpdate)
            }

        })

    }

    override fun onMapReady(map : NaverMap) {

        naverMap = map

        naverMap.minZoom = 10.00
        naverMap.maxZoom = 18.00

        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = false

        binding.currentLocationButton.map = naverMap

        locationSource = FusedLocationSource(this@HomeFragment , LOCATION_PERMISSION_REQUEST_CODE)

        naverMap.locationSource = locationSource

        /** 화면 전환 전에 보고 있던 cameraPosition 이 존재한다면 */
        if(sharedPreferences != null) {
            /** 위도 , 경도 , 줌 가져오기 */
            val cameraPosition = CameraPosition(LatLng(sharedPreferences?.getString("latitude" , "")!!.toDouble() ,
                sharedPreferences?.getString("longitude" ,"")!!.toDouble()),
                sharedPreferences?.getString("zoom" ,"")!!.toDouble()
            )
            /** 다시 재배치 */
            naverMap.cameraPosition = cameraPosition

        }
        /** 처음 화면을 보는 거라면 */
        else {

            val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.497933, 127.027558))
            naverMap.moveCamera(cameraUpdate)
        }


        naverClustering =  TedNaverClustering.with<ItemEntity>(requireActivity(), naverMap)
            .customMarker {
                    clusterItem ->
                Marker().apply {
                    icon = MarkerIcons.BLACK
                    iconTintColor = Color.RED
                    tag = clusterItem.id
                }
            }.markerClickListener { item ->
                /** 해당 item 으로 viewPager 이동 */
                val itemPosition = viewPagerAdapter.currentList.indexOf(item)
                binding.itemViewPager.currentItem = itemPosition
            }
            .items(itemList)
            .make()

        /** 지도 클릭 이벤트 관련 */
        mapOnClick()

    }


    private fun mapOnClick() {
        /** 지도 클릭 시 */
        naverMap.setOnMapClickListener { pointF, latLng ->

            //Toast.makeText(context , "위도 : ${latLng.latitude} , 경도 : ${latLng.longitude}" , Toast.LENGTH_SHORT).show()

            /** 물품 등록 창으로 이동 */
            val intent = Intent(requireActivity(), ItemInsertActivity::class.java)
            intent.putExtra(ItemInsertActivity.LATITUDE , latLng.latitude)
            intent.putExtra(ItemInsertActivity.LONGITUDE , latLng.longitude)
            startActivity(intent)
            requireActivity()?.let {
                it.overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left)
            }
        }

    }

    private fun clearMarker() {
        naverClustering.clearItems()
    }

    private fun updateMarker(itemList : MutableList<ItemEntity>)  = with(binding) {

//        itemList.forEach { item ->
//            val marker = Marker()
//            marker.position = LatLng(item.latitude , item.longitude)
//            marker.icon = MarkerIcons.BLACK
//            marker.iconTintColor = Color.RED
//
//            /** 마커 식별 태그 지정 */
//            marker.tag = item.id
//
//            /** 마커 클릭 시 */
//            marker.setOnClickListener(object : Overlay.OnClickListener {
//                override fun onClick(overlay: Overlay): Boolean {
//                    /** 해당 item 으로 viewPager 이동 */
//                    val itemPosition = viewPagerAdapter.currentList.indexOf(item)
//                    itemViewPager.currentItem = itemPosition
//                    return true
//                }
//            })
//
//            if(this@HomeFragment::naverMap.isInitialized) marker.map = naverMap
//        }

        if(this@HomeFragment::naverMap.isInitialized) {

            context?.let {

                TedNaverClustering.with<ItemEntity>(it, naverMap)
                    .customMarker {
                            clusterItem ->
                        Marker().apply {
                            icon = MarkerIcons.BLACK
                            iconTintColor = Color.RED
                            tag = clusterItem.id
                        }
                    }.markerClickListener { item ->
                        /** 해당 item 으로 viewPager 이동 */
                        val itemPosition = viewPagerAdapter.currentList.indexOf(item)
                        itemViewPager.currentItem = itemPosition
                    }
                    .items(itemList)
                    .make()

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if(locationSource.onRequestPermissionsResult(requestCode , permissions , grantResults))  {

           if(!locationSource.isActivated) {

               naverMap.locationTrackingMode = LocationTrackingMode.None
           }

        }

    }

    private fun createChatRoom(itemEntity : ItemEntity) = with(binding) {

        if(auth.currentUser?.uid == itemEntity.sellerId) {
            context?.let {
                it.toast("자신이 등록한 아이템입니다.")
            }
        }

        else {
                auth.currentUser?.let { user ->
                    /** 채팅방 개설 */
                    val chatRoom = ChatRoomItem(
                        key = "${user.uid}${itemEntity.sellerId}${System.currentTimeMillis()}",
                        title = itemEntity.title,
                        user.uid,
                        itemEntity.sellerId,
                        user.email as String,
                        itemEntity.sellerEmail,
                        )

                    /** buyer , seller 밑에 child 로 채팅방 생성 */
                    userDB.child(user.uid)
                        .child(DBKey.CHAT)
                        .push()
                        .setValue(chatRoom)

                    userDB.child(itemEntity.sellerId)
                        .child(DBKey.CHAT)
                        .push()
                        .setValue(chatRoom)

                    Snackbar.make(this@HomeFragment.view as View , "채팅방이 생성되었습니다. 채팅탭에서 확인해주세요." , Snackbar.LENGTH_LONG).show()

                }

        }

    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

        /** 상품 목록 불러오기 */
        itemsDB.addListenerForSingleValueEvent(itemListener)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()

        itemsDB.removeEventListener(itemListener)

        /** fragment 화면전환 시 보고 있던 cameraPosition  sharedPreference에 저장 */
        context?.let {

            sharedPreferences = it.getSharedPreferences("camera_position" , Context.MODE_PRIVATE)
            sharedPreferences?.edit(true) {
                putString("latitude" , naverMap.cameraPosition.target.latitude.toString())
                putString("longitude" , naverMap.cameraPosition.target.longitude.toString())
                putString("zoom" , naverMap.cameraPosition.zoom.toString())
            }
        }

    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        itemsDB.removeEventListener(itemListener)
        _binding = null
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}