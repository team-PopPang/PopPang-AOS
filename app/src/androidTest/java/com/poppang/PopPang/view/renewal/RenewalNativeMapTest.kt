package com.poppang.PopPang.view.renewal

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.rememberCameraPositionState
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.view.MapView
import com.poppang.PopPang.viewmodel.MapViewModel
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

/** Exercises the real native marker bitmap and tap without an account or popup API. */
class RenewalNativeMapTest {
    @get:Rule val compose = createComposeRule()

    @OptIn(ExperimentalNaverMapApi::class)
    @Test fun nativeRenewalMarkerCanBeSelectedAndRedrawn() {
        assumeTrue("Requires a test emulator with the documented fixed mock location", InstrumentationRegistry.getArguments().getString("renewalNativeMap") == "true")
        val popup = Gson().fromJson(
            """{"popupUuid":"native-preview","name":"리뉴얼 마커","startDate":"2026-11-01","endDate":"2026-11-30","address":"울산","region":"울산","latitude":35.5384,"longitude":129.3114,"imageUrlList":[],"captionSummary":"","favoriteCount":0,"viewCount":0,"isFavorited":false}""",
            PopupEvent::class.java,
        )
        val latest = AtomicReference<Location?>()
        var selected by mutableStateOf<String?>(null)
        lateinit var camera: CameraPositionState
        compose.setContent {
            camera = rememberCameraPositionState { position = CameraPosition(LatLng(35.5384, 129.3114), 14.0) }
            val model: MapViewModel = viewModel()
            LaunchedEffect(model) { model.setPopups(listOf(popup)) }
            MapView(model, camera, { _, item -> selected = item?.popupUuid }, { latest.set(it) }, selectedPopupUuid = selected, renewalMarkers = true)
        }
        compose.waitUntil(30_000) {
            latest.get() != null && kotlin.math.abs(camera.position.target.latitude - 35.5384) < .0005 &&
                kotlin.math.abs(camera.position.target.longitude - 129.3114) < .0005
        }
        compose.waitForIdle()
        compose.onRoot().performTouchInput { click(Offset(width / 2f, height / 2f)) }
        compose.waitUntil(10_000) { selected == popup.popupUuid }
        compose.runOnIdle { assertEquals("native-preview", selected) }
        compose.waitForIdle()
    }
}
