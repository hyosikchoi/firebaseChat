package kr.co.chat.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View

import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import kr.co.chat.R
import kr.co.chat.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home)  , OnMapReadyCallback {

    private lateinit var binding : FragmentHomeBinding

    private lateinit var naverMap : NaverMap

    private lateinit var locationSource: FusedLocationSource

    /** 카메라 위치 저장하기 위한 preference */
    private var sharedPreferences: SharedPreferences ?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync(this)


    }

    override fun onMapReady(map : NaverMap) {

        naverMap = map

        naverMap.minZoom = 12.00
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

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()

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
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}