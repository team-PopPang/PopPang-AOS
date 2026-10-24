package com.poppang.PopPang

import android.location.Location
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.rememberCameraPositionState
import com.poppang.PopPang.view.MapView
import com.poppang.PopPang.viewmodel.MapViewModel
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in real-device camera test. No popup/account API calls or coordinate logging. */
@RunWith(AndroidJUnit4::class)
class MapLocationInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalNaverMapApi::class)
    @Test
    fun firstFixCentersAndLocationButtonRestoresFollowOnRepeatedTaps() {
        assumeTrue("Requires -e liveMap true with location permission and device location enabled", InstrumentationRegistry.getArguments().getString("liveMap") == "true")
        val latestLocation = AtomicReference<Location?>()
        lateinit var camera: CameraPositionState
        composeRule.setContent {
            camera = rememberCameraPositionState { position = CameraPosition(LatLng(37.5665, 126.9780), 14.0) }
            val mapViewModel: MapViewModel = viewModel()
            MapView(
                mapviewmodel = mapViewModel,
                cameraPositionState = camera,
                setShowDetail = { _, _ -> },
                onLocationUpdated = { latestLocation.set(Location(it)) },
            )
        }
        fun distanceFromLatest(): Float {
            val location = latestLocation.get() ?: return Float.POSITIVE_INFINITY
            val distance = FloatArray(1)
            val target = camera.position.target
            Location.distanceBetween(target.latitude, target.longitude, location.latitude, location.longitude, distance)
            return distance[0]
        }
        composeRule.waitUntil(30_000) { latestLocation.get() != null && distanceFromLatest() < 200 }
        composeRule.runOnIdle { assertEquals(LocationTrackingMode.Follow, camera.locationTrackingMode) }

        repeat(2) {
            composeRule.runOnIdle {
                val location = checkNotNull(latestLocation.get())
                camera.move(CameraUpdate.toCameraPosition(CameraPosition(LatLng(location.latitude + 0.03, location.longitude + 0.03), 14.0)))
            }
            composeRule.waitUntil(5_000) { distanceFromLatest() > 1_000 }
            composeRule.runOnIdle { assertEquals(LocationTrackingMode.NoFollow, camera.locationTrackingMode) }
            composeRule.onNodeWithContentDescription("내 위치").performClick()
            composeRule.waitUntil(15_000) { distanceFromLatest() < 200 }
            composeRule.runOnIdle {
                assertTrue("Camera did not return to the latest location", distanceFromLatest() < 200)
                assertEquals(LocationTrackingMode.Follow, camera.locationTrackingMode)
            }
        }
    }
}
