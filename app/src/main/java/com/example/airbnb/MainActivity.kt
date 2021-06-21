package com.example.airbnb

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val mapView: MapView by lazy {
        findViewById<MapView>(R.id.mapView)
    }

    private val viewPager: ViewPager2 by lazy {
        findViewById(R.id.houseViewPager)
    }

    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.recyclerView)
    }

    //현위치 버튼 임의로 가려져서 임의로 민듦
    private val currentLocationButton: LocationButtonView by lazy {
        findViewById(R.id.currentLocationButton)
    }

    private val bottomSheetTitleTextView : TextView by lazy{
        findViewById<TextView>(R.id.bottomSheetTitleTextView)
    }

    private val viewPagerAdapter = HoseViewPagerAdapter(itemClicked = {
        val intent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "[지금 이 가격에 예약하세요] ${it.title} ${it.price} ${it.imageUrl}") // 보통은 링크를 넣어서 딥링크로 숙소 보여주는..?
                type = "text/plain"
            }
        startActivity(Intent.createChooser(intent,null))
    })
    private val recyclerAdapter = HouseListAdapter()

    //맵뷰 라이프사이클 연결해주어야 함
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)

        // 지도가져오고 -> 마커 찍음( 동기로 이루어짐)

        //콜백을 통해서 naverMap 객체를 얻을 수 있다.
        mapView.getMapAsync(this) // -> onmapready 콜백으로 실행되는..?
        viewPager.adapter = viewPagerAdapter
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //페이지 넘길때 호출되는 메소드
        //해당 콜백은 abstract static 이라서 모든 구현체를 구현하지 않아도 괜찮다
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedHouseModel = viewPagerAdapter.currentList[position]
                val cameraUpdate =
                    CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat, selectedHouseModel.lng))
                        .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    //클래스에 인터페이스 구현하고 여기서 이제 메소드를 구현하게 되면
    //메인 액티비티 자체가 onmapready에 대한 구현체라고도 할 수 있음
    //그래서 getMapAsyc 에 this를 넣을 수 있는 것
    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.maxZoom = 19.0
        naverMap.minZoom = 10.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.360792, 127.119515)) // 위 경도 주소로
        naverMap.moveCamera(cameraUpdate) // 지도를 켰을 때 처음에 설정할 위치를 이렇게 설정함

        //현위치 버튼
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false //지도에서 안보이게 비활성화

        currentLocationButton.map = naverMap

        //위치같은 민감한 정보는 권한 설정 해주어야함 - manifest
        //6.0 이후부터는 권한 동의 선택도 필요함 -> 구글 로케이션을 gradle 에 추가하여 편하게 설정함
        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getHouseListFromAPI()

        //임의로 찍었던 마커
        /*    val marker = Marker()
            marker.position = LatLng(37.363124, 127.109317)
            marker.map = naverMap
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.LTGRAY*/
    }

    private fun getHouseListFromAPI() {
        //레트로핏 객체 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HouseService::class.java).also {
            // 만들고 이후에 -- also
            it.getHouseList()
                .enqueue(object : Callback<HouseDto> {
                    override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                        if (response.isSuccessful.not()) {
                            return
                        }

                        response.body()?.let { dto ->
                            updateMarker(dto.items)
                            viewPagerAdapter.submitList(dto.items)
                            recyclerAdapter.submitList(dto.items)

                            bottomSheetTitleTextView.text = "${dto.items.size} 개의 숙소"
                        }
                    }

                    override fun onFailure(call: Call<HouseDto>, t: Throwable) {

                    }

                })
        }

    }

    private fun updateMarker(houses: List<HouseModel>) {
        houses.forEach { house ->
            val marker = Marker()
            marker.position = LatLng(house.lat, house.lng)
            marker.onClickListener = this
            marker.map = naverMap
            marker.tag = house.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED
        }
    }

    // 구글 라이브러리로 편하게 팝업 사용
    //권한 요청했으니 이것도 구현해야 함
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onClick(overlay: Overlay): Boolean {
        //마커 총 집합 -> overlay
        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            //제일 먼저 나오는 아이템 찾고 없으면 null 반환
            it.id == overlay.tag
        }
        selectedModel?.let{
           val position = viewPagerAdapter.currentList.indexOf(it)
            viewPager.currentItem = position
        }
        return true // onclick에 대한 값 리턴
    }
}
