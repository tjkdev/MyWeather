package com.example.myweather.util

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Looper
import androidx.activity.ComponentActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.*

@SuppressLint("MissingPermission")
class LocationUtil(
    private val activity: ComponentActivity,
    private val latLngCallback: (Address) -> Unit
) {

    private val updateIntervalInMilliseconds: Long = 1000 * 60 * 60
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(activity)
    }
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(updateIntervalInMilliseconds)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
    }
    private val locationSettingsRequest: LocationSettingsRequest by lazy {
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
    }
    private val settingsClient: SettingsClient by lazy {
        LocationServices.getSettingsClient(activity)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationResult.lastLocation?.let {
                val geo = Geocoder(activity, Locale.KOREA).getFromLocation(it.latitude, it.longitude, 1)
                if (!geo.isNullOrEmpty()) {
                    val address = getAddress(convertGoogleAddress(geo[0].getAddressLine(0)))
                    address?.let {
                        latLngCallback.invoke(it)
                    }
                }
            }
        }
    }

    fun getLocation() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
            .addOnFailureListener {
                val gmsApi = it as? ApiException
                gmsApi?.let { gms ->
                    when (gms.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            val rae = it as? ResolvableApiException
                            rae?.startResolutionForResult(activity, 0)
                        }
                        else -> {}
                    }
                }
            }
    }

    private fun convertGoogleAddress(googleAddress: String): String {
        val stringArray = googleAddress.split(" ")
        return if (stringArray[1] == "충청남도" && stringArray[2] == "연기군") {
            "세종특별자치시 세종특별자치시"
        } else {
            when (stringArray[2]) {
                "수원시",
                "성남시",
                "안양시",
                "안산시",
                "고양시",
                "용인시",
                "청주시",
                "천안시",
                "전주시",
                "포항시",
                "창원시" -> "${stringArray[1]} ${stringArray[2]} ${stringArray[3]}"
                else -> "${stringArray[1]} ${stringArray[2]}"
            }
        }
    }

    private fun getAddress(address: String): Address? {
        val addressData = addressList.filter { address == it.address }
        return if (addressData.isNotEmpty()) addressData[0] else null
    }

    private val addressList = listOf(
        Address("서울특별시 종로구", 60, 127),
        Address("서울특별시 중구", 60, 127),
        Address("서울특별시 용산구", 60, 126),
        Address("서울특별시 성동구", 61, 127),
        Address("서울특별시 광진구", 62, 126),
        Address("서울특별시 동대문구", 61, 127),
        Address("서울특별시 중랑구", 62, 128),
        Address("서울특별시 성북구", 61, 127),
        Address("서울특별시 강북구", 61, 128),
        Address("서울특별시 도봉구", 61, 129),
        Address("서울특별시 노원구", 61, 129),
        Address("서울특별시 은평구", 59, 127),
        Address("서울특별시 서대문구", 59, 127),
        Address("서울특별시 마포구", 59, 127),
        Address("서울특별시 양천구", 58, 126),
        Address("서울특별시 강서구", 58, 126),
        Address("서울특별시 구로구", 58, 125),
        Address("서울특별시 금천구", 59, 124),
        Address("서울특별시 영등포구", 58, 126),
        Address("서울특별시 동작구", 59, 125),
        Address("서울특별시 관악구", 59, 125),
        Address("서울특별시 서초구", 61, 125),
        Address("서울특별시 강남구", 61, 126),
        Address("서울특별시 송파구", 62, 126),
        Address("서울특별시 강동구", 62, 126),
        Address("부산광역시 중구", 97, 74),
        Address("부산광역시 서구", 97, 74),
        Address("부산광역시 동구", 98, 75),
        Address("부산광역시 영도구", 98, 74),
        Address("부산광역시 부산진구", 97, 75),
        Address("부산광역시 동래구", 98, 76),
        Address("부산광역시 남구", 98, 75),
        Address("부산광역시 북구", 96, 76),
        Address("부산광역시 해운대구", 99, 75),
        Address("부산광역시 사하구", 96, 74),
        Address("부산광역시 금정구", 98, 77),
        Address("부산광역시 강서구", 96, 76),
        Address("부산광역시 연제구", 98, 76),
        Address("부산광역시 수영구", 99, 75),
        Address("부산광역시 사상구", 96, 75),
        Address("부산광역시 기장군", 100, 77),
        Address("대구광역시 중구", 89, 90),
        Address("대구광역시 동구", 90, 91),
        Address("대구광역시 서구", 88, 90),
        Address("대구광역시 남구", 89, 90),
        Address("대구광역시 북구", 89, 91),
        Address("대구광역시 수성구", 89, 90),
        Address("대구광역시 달서구", 88, 90),
        Address("대구광역시 달성군", 86, 88),
        Address("인천광역시 중구", 54, 125),
        Address("인천광역시 동구", 54, 125),
        Address("인천광역시 미추홀구", 54, 124),
        Address("인천광역시 연수구", 55, 123),
        Address("인천광역시 남동구", 56, 124),
        Address("인천광역시 부평구", 55, 125),
        Address("인천광역시 계양구", 56, 126),
        Address("인천광역시 서구", 55, 126),
        Address("인천광역시 강화군", 51, 130),
        Address("인천광역시 옹진군", 54, 124),
        Address("광주광역시 동구", 60, 74),
        Address("광주광역시 서구", 59, 74),
        Address("광주광역시 남구", 59, 73),
        Address("광주광역시 북구", 59, 75),
        Address("광주광역시 광산구", 57, 74),
        Address("대전광역시 동구", 68, 100),
        Address("대전광역시 중구", 68, 100),
        Address("대전광역시 서구", 67, 100),
        Address("대전광역시 유성구", 67, 101),
        Address("대전광역시 대덕구", 68, 100),
        Address("울산광역시 중구", 102, 84),
        Address("울산광역시 남구", 102, 84),
        Address("울산광역시 동구", 104, 83),
        Address("울산광역시 북구", 103, 85),
        Address("울산광역시 울주군", 101, 84),
        Address("세종특별자치시 세종특별자치시", 66, 103),
        Address("경기도 수원시 장안구", 60, 121),
        Address("경기도 수원시 권선구", 60, 120),
        Address("경기도 수원시 팔달구", 61, 121),
        Address("경기도 수원시 영통구", 61, 120),
        Address("경기도 성남시 수정구", 63, 124),
        Address("경기도 성남시 중원구", 63, 124),
        Address("경기도 성남시 분당구", 62, 123),
        Address("경기도 의정부시", 61, 130),
        Address("경기도 안양시 만안구", 59, 123),
        Address("경기도 안양시 동안구", 59, 123),
        Address("경기도 부천시", 56, 125),
        Address("경기도 광명시", 58, 125),
        Address("경기도 평택시", 62, 114),
        Address("경기도 동두천시", 61, 134),
        Address("경기도 안산시 상록구", 58, 121),
        Address("경기도 안산시 단원구", 57, 121),
        Address("경기도 고양시 덕양구", 57, 128),
        Address("경기도 고양시 일산동구", 56, 129),
        Address("경기도 고양시 일산서구", 56, 129),
        Address("경기도 과천시", 60, 124),
        Address("경기도 구리시", 62, 127),
        Address("경기도 남양주시", 64, 128),
        Address("경기도 오산시", 62, 118),
        Address("경기도 시흥시", 57, 123),
        Address("경기도 군포시", 59, 122),
        Address("경기도 의왕시", 60, 122),
        Address("경기도 하남시", 64, 126),
        Address("경기도 용인시 처인구", 64, 119),
        Address("경기도 용인시 기흥구", 62, 120),
        Address("경기도 용인시 수지구", 62, 121),
        Address("경기도 파주시", 56, 131),
        Address("경기도 이천시", 68, 121),
        Address("경기도 안성시", 65, 115),
        Address("경기도 김포시", 55, 128),
        Address("경기도 화성시", 57, 119),
        Address("경기도 광주시", 65, 123),
        Address("경기도 양주시", 61, 131),
        Address("경기도 포천시", 64, 134),
        Address("경기도 여주시", 71, 121),
        Address("경기도 연천군", 61, 138),
        Address("경기도 가평군", 69, 133),
        Address("경기도 양평군", 69, 125),
        Address("강원도 춘천시", 73, 134),
        Address("강원도 원주시", 76, 122),
        Address("강원도 강릉시", 92, 131),
        Address("강원도 동해시", 97, 127),
        Address("강원도 태백시", 95, 119),
        Address("강원도 속초시", 87, 141),
        Address("강원도 삼척시", 98, 125),
        Address("강원도 홍천군", 75, 130),
        Address("강원도 횡성군", 77, 125),
        Address("강원도 영월군", 86, 119),
        Address("강원도 평창군", 84, 123),
        Address("강원도 정선군", 89, 123),
        Address("강원도 철원군", 65, 139),
        Address("강원도 화천군", 72, 139),
        Address("강원도 양구군", 77, 139),
        Address("강원도 인제군", 80, 138),
        Address("강원도 고성군", 85, 145),
        Address("강원도 양양군", 88, 138),
        Address("충청북도 청주시 상당구", 69, 106),
        Address("충청북도 청주시 서원구", 69, 107),
        Address("충청북도 청주시 흥덕구", 67, 106),
        Address("충청북도 청주시 청원구", 69, 107),
        Address("충청북도 충주시", 76, 114),
        Address("충청북도 제천시", 81, 118),
        Address("충청북도 보은군", 73, 103),
        Address("충청북도 옥천군", 71, 99),
        Address("충청북도 영동군", 74, 97),
        Address("충청북도 증평군", 71, 110),
        Address("충청북도 진천군", 68, 111),
        Address("충청북도 괴산군", 74, 111),
        Address("충청북도 음성군", 72, 113),
        Address("충청북도 단양군", 84, 115),
        Address("충청남도 천안시 동남구", 63, 110),
        Address("충청남도 천안시 서북구", 63, 112),
        Address("충청남도 공주시", 63, 102),
        Address("충청남도 보령시", 54, 100),
        Address("충청남도 아산시", 60, 110),
        Address("충청남도 서산시", 51, 110),
        Address("충청남도 논산시", 62, 97),
        Address("충청남도 계룡시", 65, 99),
        Address("충청남도 당진시", 54, 112),
        Address("충청남도 금산군", 69, 95),
        Address("충청남도 부여군", 59, 99),
        Address("충청남도 서천군", 55, 94),
        Address("충청남도 청양군", 57, 103),
        Address("충청남도 홍성군", 55, 106),
        Address("충청남도 예산군", 58, 107),
        Address("충청남도 태안군", 48, 109),
        Address("전라북도 전주시 완산구", 63, 89),
        Address("전라북도 전주시 덕진구", 63, 89),
        Address("전라북도 군산시", 56, 92),
        Address("전라북도 익산시", 60, 91),
        Address("전라북도 정읍시", 58, 83),
        Address("전라북도 남원시", 68, 80),
        Address("전라북도 김제시", 59, 88),
        Address("전라북도 완주군", 63, 89),
        Address("전라북도 진안군", 68, 88),
        Address("전라북도 무주군", 72, 93),
        Address("전라북도 장수군", 70, 85),
        Address("전라북도 임실군", 66, 84),
        Address("전라북도 순창군", 63, 79),
        Address("전라북도 고창군", 56, 80),
        Address("전라북도 부안군", 56, 87),
        Address("전라남도 목포시", 50, 67),
        Address("전라남도 여수시", 73, 66),
        Address("전라남도 순천시", 70, 70),
        Address("전라남도 나주시", 56, 71),
        Address("전라남도 광양시", 73, 70),
        Address("전라남도 담양군", 61, 78),
        Address("전라남도 곡성군", 66, 77),
        Address("전라남도 구례군", 69, 75),
        Address("전라남도 고흥군", 66, 62),
        Address("전라남도 보성군", 62, 66),
        Address("전라남도 화순군", 61, 72),
        Address("전라남도 장흥군", 59, 64),
        Address("전라남도 강진군", 57, 63),
        Address("전라남도 해남군", 54, 61),
        Address("전라남도 영암군", 56, 66),
        Address("전라남도 무안군", 52, 71),
        Address("전라남도 함평군", 52, 72),
        Address("전라남도 영광군", 52, 77),
        Address("전라남도 장성군", 57, 77),
        Address("전라남도 완도군", 57, 56),
        Address("전라남도 진도군", 48, 59),
        Address("전라남도 신안군", 50, 66),
        Address("경상북도 포항시 남구", 102, 94),
        Address("경상북도 포항시 북구", 102, 95),
        Address("경상북도 경주시", 100, 91),
        Address("경상북도 김천시", 80, 96),
        Address("경상북도 안동시", 91, 106),
        Address("경상북도 구미시", 84, 96),
        Address("경상북도 영주시", 89, 111),
        Address("경상북도 영천시", 95, 93),
        Address("경상북도 상주시", 81, 102),
        Address("경상북도 문경시", 81, 106),
        Address("경상북도 경산시", 91, 90),
        Address("경상북도 군위군", 88, 99),
        Address("경상북도 의성군", 90, 101),
        Address("경상북도 청송군", 96, 103),
        Address("경상북도 영양군", 97, 108),
        Address("경상북도 영덕군", 102, 103),
        Address("경상북도 청도군", 91, 86),
        Address("경상북도 고령군", 83, 87),
        Address("경상북도 성주군", 83, 91),
        Address("경상북도 칠곡군", 85, 93),
        Address("경상북도 예천군", 86, 107),
        Address("경상북도 봉화군", 90, 113),
        Address("경상북도 울진군", 102, 115),
        Address("경상북도 울릉군", 127, 127),
        Address("경상남도 창원시 의창구", 90, 77),
        Address("경상남도 창원시 성산구", 91, 76),
        Address("경상남도 창원시 마산합포구", 89, 76),
        Address("경상남도 창원시 마산회원구", 89, 76),
        Address("경상남도 창원시 진해구", 91, 75),
        Address("경상남도 진주시", 81, 75),
        Address("경상남도 통영시", 87, 68),
        Address("경상남도 사천시", 80, 71),
        Address("경상남도 김해시", 95, 77),
        Address("경상남도 밀양시", 92, 83),
        Address("경상남도 거제시", 90, 69),
        Address("경상남도 양산시", 97, 79),
        Address("경상남도 의령군", 83, 78),
        Address("경상남도 함안군", 86, 77),
        Address("경상남도 창녕군", 87, 83),
        Address("경상남도 고성군", 85, 71),
        Address("경상남도 남해군", 77, 68),
        Address("경상남도 하동군", 74, 73),
        Address("경상남도 산청군", 76, 80),
        Address("경상남도 함양군", 74, 82),
        Address("경상남도 거창군", 77, 86),
        Address("경상남도 합천군", 81, 84),
        Address("제주특별자치도 제주시", 53, 38),
        Address("제주특별자치도 서귀포시", 52, 33)
    )
}

data class Address(
    val address: String,
    val x: Int,
    val y: Int
)