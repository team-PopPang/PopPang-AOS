package com.poppang.PopPang.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.naver.maps.map.LocationSource
import com.naver.maps.map.location.FusedLocationSource

internal data class MapLocationControls(
    val source: LocationSource,
    val permissionGranted: Boolean,
    val onLocationChanged: (Location) -> Unit,
    val requestCenter: () -> Unit,
    val cancelPendingCenter: () -> Unit,
)

@SuppressLint("MissingPermission") // Both location permissions are checked before each request.
@Composable
internal fun rememberMapLocationControls(
    onLocationUpdated: (Location) -> Unit,
    onCenter: (MapLocationFix) -> Unit,
    onFollowRequested: () -> Unit,
): MapLocationControls {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { MapLocationState() }
    val client = remember(context) { LocationServices.getFusedLocationProviderClient(context) }
    val locationManager = remember(context) { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var permissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var refreshRequest by remember { mutableIntStateOf(0) }
    var isResumed by remember { mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    val updateLocation by rememberUpdatedState(onLocationUpdated)
    val center by rememberUpdatedState(onCenter)
    val follow by rememberUpdatedState(onFollowRequested)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissionGranted = hasLocationPermission(context)
        if (!permissionGranted) {
            Toast.makeText(context, "현재 위치를 사용하려면 앱 설정에서 위치 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
        }
    }
    val requestPermission = {
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    val source = remember(context) {
        object : FusedLocationSource(context) {
            // Approximate location is sufficient; the library's default helper requires fine too.
            override fun hasPermissions() = hasLocationPermission(context)
            override fun onPermissionRequest() = requestPermission()
        }
    }
    val acceptLocation: (Location) -> Unit = { location ->
        val now = SystemClock.elapsedRealtimeNanos()
        if (state.update(MapLocationFix(location.latitude, location.longitude, location.elapsedRealtimeNanos), now)) {
            updateLocation(location)
            state.consumeCenter(now)?.let { center(it) }
        }
    }
    val latestAcceptLocation by rememberUpdatedState(acceptLocation)

    LaunchedEffect(Unit) {
        if (!permissionGranted) requestPermission()
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isResumed = true
                permissionGranted = hasLocationPermission(context)
                refreshRequest += 1
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                isResumed = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(permissionGranted, refreshRequest, isResumed) {
        var active = true
        val cancellation = CancellationTokenSource()
        if (isResumed && permissionGranted && LocationManagerCompat.isLocationEnabled(locationManager)) {
            // Show a recent cached fix immediately, then refresh it without a 5-second polling delay.
            client.lastLocation.addOnSuccessListener { location ->
                if (active && location != null) latestAcceptLocation(location)
            }
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
                .addOnSuccessListener { location ->
                    if (active) {
                        if (location != null) latestAcceptLocation(location)
                        if (state.isCenterPending) {
                            Toast.makeText(context, "현재 위치를 확인하지 못했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .addOnFailureListener {
                    if (active && state.isCenterPending) {
                        Toast.makeText(context, "현재 위치를 확인하지 못했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                    }
                }
        }
        onDispose {
            active = false
            cancellation.cancel()
        }
    }

    return MapLocationControls(
        source = source,
        permissionGranted = permissionGranted,
        onLocationChanged = acceptLocation,
        requestCenter = {
            state.requestCenter()
            permissionGranted = hasLocationPermission(context)
            if (!permissionGranted) {
                requestPermission()
            } else if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                Toast.makeText(context, "기기의 위치 기능을 켜주세요.", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                val cached = state.consumeCenter(SystemClock.elapsedRealtimeNanos())
                cached?.let { center(it) }
                // CameraUpdate switches the SDK to NoFollow; restore Follow after moving, on every tap.
                follow()
                refreshRequest += 1
                if (cached == null) Toast.makeText(context, "현재 위치를 확인하고 있습니다.", Toast.LENGTH_SHORT).show()
            }
        },
        cancelPendingCenter = state::cancelPendingCenter,
    )
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
