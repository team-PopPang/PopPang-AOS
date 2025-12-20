package com.poppang.PopPang.view

import android.annotation.SuppressLint
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.DisposableMapEffect
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.poppang.PopPang.R
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Bold17
import com.poppang.PopPang.ui.theme.Bold18
import com.poppang.PopPang.ui.theme.Light10
import com.poppang.PopPang.ui.theme.Light12
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.Regular15
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray3
import com.poppang.PopPang.ui.theme.mainGray4
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.MapPopupfilterViewModel
import com.poppang.PopPang.viewmodel.MapViewModel
import com.poppang.PopPang.viewmodel.RegionsViewModel
import com.poppang.PopPang.viewmodel.SelectPopupViewModel
import com.poppang.PopPang.viewmodel.ViewCountViewModel
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffold
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetState
import kotlinx.coroutines.launch

enum class MapSheetValue {
    Hidden,
    Middle,
    Expanded
}

@OptIn(ExperimentalNaverMapApi::class, ExperimentalFoundationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    popupprogressList: List<PopupEvent>,
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    loginResponse: LoginResponse?,
    favoriteViewModel: FavoriteViewModel,
    mapviewmodel: MapViewModel = viewModel(),
    viewCountViewModel: ViewCountViewModel = viewModel(),
    regionsViewModel: RegionsViewModel,
    mapPopupfilterViewModel: MapPopupfilterViewModel = viewModel(),
    selectPopupViewModel: SelectPopupViewModel
) {
    var selectedRegion by remember { mutableStateOf("전체") }
    var selectedDistrict by remember { mutableStateOf("전체") }
    var selectedSort by remember { mutableStateOf("CLOSEST") }
    val context = LocalContext.current
    var latitude by remember { mutableStateOf(37.5665) }
    var longitude by remember { mutableStateOf(126.9780) }
    val mappopupfilterList by mapPopupfilterViewModel.mapPopupfilterList.collectAsState()
    val searchedPopups by mapviewmodel.searchedPopups.collectAsState(initial = emptyList())
    val isSearched = remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(latitude, longitude), 14.0)
    }
    val coroutineScope = rememberCoroutineScope()
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    val selectPopupList by selectPopupViewModel.selectpopupList.collectAsState()
    var detailPopup by remember { mutableStateOf<PopupEvent?>(null) }
    var selectedDetailPopup by remember { mutableStateOf<PopupEvent?>(null) }
    val detailSheetState = rememberBottomSheetState(
        initialValue = MapSheetValue.Middle,
        defineValues = {
            MapSheetValue.Hidden at height(percent = 0)
            MapSheetValue.Middle at height(percent = 60)
            MapSheetValue.Expanded at height(percent = 90)
        }
    )
    val detailScaffoldState = rememberBottomSheetScaffoldState(detailSheetState)

    LaunchedEffect(popupprogressList) { mapviewmodel.setPopups(popupprogressList) }

    LaunchedEffect(showDetail, selectedPopup) {
        if (showDetail && selectedPopup != null) {
            selectPopupViewModel.SelectPopupEvents(
                userUuid = loginResponse?.userUuid.orEmpty(),
                popupUuid = selectedPopup!!.popupUuid
            )
        }
    }

    LaunchedEffect(selectPopupList, showDetail, selectedPopup) {
        if (showDetail && selectedPopup != null) {
            detailPopup = selectPopupList.firstOrNull { it.popupUuid == selectedPopup!!.popupUuid }
        }
    }
    DisposableEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    Log.e("MapScreen", "Updated location: ($latitude, $longitude)")
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(
        selectedRegion,
        selectedDistrict,
        selectedSort,
        latitude,
        longitude,
        loginResponse?.userUuid
    ) {
        mapPopupfilterViewModel.fetchmappopupfilter(
            userUuid = loginResponse?.userUuid.orEmpty(),
            region = selectedRegion,
            district = selectedDistrict,
            longitude = longitude,
            latitude = latitude,
            mapSortStandard = selectedSort
        )
    }
    LaunchedEffect(selectedDetailPopup) {
        if (selectedDetailPopup != null) {
            detailSheetState.animateTo(MapSheetValue.Middle)
        }
    }
    val listToShow =
        if (isSearched.value && searchedPopups.isNotEmpty()) searchedPopups else mappopupfilterList
    val sheetState = rememberBottomSheetState(
        initialValue = MapSheetValue.Middle,
        defineValues = {
            MapSheetValue.Hidden at height(percent = 0)
            MapSheetValue.Middle at height(percent = 45)
            MapSheetValue.Expanded at height(percent = 85)
        }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = Color.White,
        sheetShadowElevation = 8.dp,
        sheetTonalElevation = 8.dp,
        sheetDragHandle = {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = mainBlack,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .align(Alignment.CenterHorizontally)
                )
            }
        },
        sheetContent = {
            MapSheetContent(
                popupList = listToShow,
                onPopupClick = { popup ->
                    cameraPositionState.move(
                        CameraUpdate.toCameraPosition(
                            CameraPosition(
                                LatLng(popup.latitude, popup.longitude),
                                14.0
                            )
                        )
                    )
                    coroutineScope.launch {
                        sheetState.animateTo(MapSheetValue.Middle)
                        if (selectedDetailPopup != null) {
                            selectedDetailPopup = null
                            selectedDetailPopup = popup
                        } else {
                            selectedDetailPopup = popup
                        }
                    }
                },
                viewCountViewModel = viewCountViewModel,
                favoriteViewModel = favoriteViewModel,
                refreshTrigger = showDetail,
                loginResponse = loginResponse,
                selectedSort = selectedSort,
                onSortChange = { sort ->
                    selectedSort = sort
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MapView(
                mapviewmodel = mapviewmodel,
                cameraPositionState = cameraPositionState,
                setShowDetail = { show, popup ->
                    setShowDetail(show)
                    selectedPopup = popup
                },
                latitude = latitude,
                longitude = longitude
            )
            MapSearchBar(
                mapViewModel = mapviewmodel,
                isSearched = isSearched,
                loginResponse = loginResponse,
                regionsViewModel = regionsViewModel,
                selectedRegion = selectedRegion,
                onRegionChange = { region, district ->
                    selectedRegion = region
                    selectedDistrict = district
                }
            )
            AnimatedVisibility(
                visible = scaffoldState.sheetState.currentValue == MapSheetValue.Hidden,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 15.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .clickable {
                            coroutineScope.launch {
                                sheetState.animateTo(MapSheetValue.Middle)
                            }
                        },
                ) {
                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.menu_icon),
                            contentDescription = "리스트 아이콘",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "목록 보기",
                            style = Regular12,
                            color = mainBlack,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDetail && detailPopup != null) {
        ContentDetail(
            popup = detailPopup!!,
            onClose = { setShowDetail(false) },
            loginResponse = loginResponse,
            favoriteViewModel = favoriteViewModel,
            showDetail = showDetail,
            setShowDetail = setShowDetail,
            selectPopupViewModel = selectPopupViewModel
        )
    }

    if (selectedDetailPopup != null) {
        BottomSheetScaffold(
            scaffoldState = detailScaffoldState,
            sheetContainerColor = Color.White,
            sheetShadowElevation = 8.dp,
            sheetTonalElevation = 8.dp,
            sheetDragHandle = {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = mainBlack,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.CenterHorizontally)
                    )
                }
            },
            sheetContent = {
                MapSheetContentDeltail(
                    popup = selectedDetailPopup!!,
                    onClose = {
                        coroutineScope.launch {
                            detailSheetState.animateTo(MapSheetValue.Hidden)
                            // 애니메이션이 끝난 뒤에 null 처리
                            selectedDetailPopup = null
                        }
                    }
                )
            }
        ) {
        }
    }
}


@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapView(
    mapviewmodel: MapViewModel,
    cameraPositionState: CameraPositionState,
    setShowDetail: (Boolean, PopupEvent?) -> Unit,
    latitude: Double,
    longitude: Double
) {
    val popups by mapviewmodel.popups.collectAsState(initial = emptyList())
    val popupMapById by mapviewmodel.popupMapById.collectAsState(initial = emptyMap())
    val iconSizeDp by mapviewmodel.iconSizeDp.collectAsState(initial = 48f)
    val cornerDp by mapviewmodel.cornerDp.collectAsState(initial = 8f)

    val locationSource = rememberFusedLocationSource()

    var trackingMode by remember { mutableStateOf(LocationTrackingMode.Follow) }
    val uiSettings = remember { MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = false) }

    val clustererState = remember { mutableStateOf<Clusterer<ItemKey>?>(null) }
    val context = LocalContext.current

    val density = context.resources.displayMetrics.density
    val iconSizePx = (iconSizeDp.toFloat() * density).toInt()
    val cornerPx = cornerDp.toFloat() * density


    Box(Modifier.fillMaxSize()) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            properties = MapProperties(locationTrackingMode = trackingMode),
            uiSettings = uiSettings
        ) {
            MapEffect(popups) { naverMap ->
                val clusterer = clustererState.value ?: Clusterer.Builder<ItemKey>()
                    .minZoom(5)
                    .maxZoom(12)
                    .leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {
                        override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                            val key = info.key as? ItemKey ?: return
                            val popup = popupMapById[key.id] ?: return
                            val url = popup.fullImageUrlList.firstOrNull() ?: return

                            marker.anchor = PointF(0.5f, 1.0f)
                            marker.captionText = ""
                            marker.subCaptionText = ""
                            marker.captionRequestedWidth = 0

                            if (url != null) {
                                val request = ImageRequest.Builder(context)
                                    .data(url)
                                    .allowHardware(false)
                                    .size(iconSizePx, iconSizePx)
                                    .transformations(RoundedCornersTransformation(cornerPx))
                                    .target(
                                        onSuccess = { drawable: Drawable ->
                                            val original = (drawable as? BitmapDrawable)?.bitmap ?: drawable.toBitmap()
                                            val bmp = if (original.width != iconSizePx || original.height != iconSizePx)
                                                original.scale(iconSizePx, iconSizePx) else original
                                            marker.icon = OverlayImage.fromBitmap(bmp)
                                        }
                                    )
                                    .build()
                                context.imageLoader.enqueue(request)
                            }
                            marker.onClickListener = Overlay.OnClickListener {
                                setShowDetail(true, popup)
                                true
                            }
                        }
                    })
                    .build()
                    .also {
                        it.map = naverMap
                        clustererState.value = it
                    }

                val keyTagMap = popups.associateBy { p ->
                    ItemKey(
                        id = p.popupUuid.hashCode(),
                        position = LatLng(p.latitude, p.longitude)
                    )
                }

                clusterer.clear()
                clusterer.addAll(keyTagMap)

            }

            DisposableMapEffect(Unit) {
                onDispose {
                    clustererState.value?.apply {
                        clear()
                        map = null
                    }
                    clustererState.value = null
                }
            }
        }
        IconButton(
            onClick = {
                cameraPositionState.move(
                    CameraUpdate.toCameraPosition(CameraPosition(LatLng(latitude, longitude), 14.0))
                )
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .size(48.dp)
                .background(Color.White, shape = RoundedCornerShape(24.dp))
                .border(1.dp, mainGray3, RoundedCornerShape(24.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.my_location_icon), // 내 위치 아이콘 리소스
                contentDescription = "내 위치",
                tint = mainOrange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun MapSheetContent(
    popupList: List<PopupEvent>,
    onPopupClick: (PopupEvent) -> Unit,
    viewCountViewModel: ViewCountViewModel,
    favoriteViewModel: FavoriteViewModel,
    refreshTrigger: Boolean,
    loginResponse: LoginResponse?,
    selectedSort: String,
    onSortChange: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(true) }
    val userUuid = loginResponse?.userUuid.orEmpty()
    val favoritePopupUuids by favoriteViewModel.favoritePopupUuids.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier.align(Alignment.End)
        ) {
            MapSortType(
                selectedSort = selectedSort,
                onSortSelected = { onSortChange(it) },
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(popupList.size) { popup ->
                val isLiked = favoritePopupUuids.contains(popupList[popup].popupUuid)
                val popup = popupList[popup]
                var favoriteCount by remember { mutableStateOf(0) }
                var viewCount by remember { mutableStateOf(0) }

                LaunchedEffect(popup.popupUuid, refreshTrigger) {
                    favoriteViewModel.getFavoriteCount(userUuid, popup.popupUuid) { count ->
                        favoriteCount = count.toInt()
                    }
                    viewCountViewModel.getTotalViewCount(userUuid, popup.popupUuid) { count ->
                        viewCount = count.toInt()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(163.dp)
                        .padding(vertical = 12.dp)
                        .background(Color.Transparent)
                        .clickable { onPopupClick(popup) }
                ) {
                    Row {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(popup.fullImageUrlList.getOrNull(0))
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = popup.name,
                            modifier = Modifier
                                .height(133.dp)
                                .width(106.dp)
                                .align(CenterVertically),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = SpaceBetween
                            ) {
                                Box {
                                    Column {
                                        Text(
                                            text = popup.roadAddress.split(" ").take(2)
                                                .joinToString(" "),
                                            style = Regular12,
                                            color = mainBlack
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = popup.name,
                                            style = Bold15,
                                            color = mainBlack,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = popup.startDateFormatted + " - " + popup.endDateFormatted,
                                            style = Regular12.copy(letterSpacing = (-1).sp),
                                            color = mainGray1
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Row(
                                        verticalAlignment = CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.eye_icon),
                                            contentDescription = "조회수 아이콘",
                                            tint = mainGray1,
                                            modifier = Modifier.padding(end = 4.dp).size(12.dp)
                                        )
                                        Text(
                                            text = viewCount.toString(),
                                            style = Regular12,
                                            color = mainGray1,
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        IconButton(
                                            onClick = {
                                                val newLikeStatus = !isLiked
                                                if (newLikeStatus) {
                                                    favoriteViewModel.addFavorite(
                                                        userUuid,
                                                        popup.popupUuid
                                                    )
                                                } else {
                                                    favoriteViewModel.deleteFavorite(
                                                        userUuid,
                                                        popup.popupUuid
                                                    )
                                                }
                                                favoriteViewModel.getFavoriteCount(
                                                    userUuid,
                                                    popup.popupUuid
                                                ) { count ->
                                                    favoriteCount = count.toInt()
                                                }
                                            },
                                            modifier = Modifier.padding(end = 4.dp).size(12.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.heart_gray_icon),
                                                contentDescription = "Like Icon",
                                                tint = if (isLiked) Color.Red else Color.Unspecified
                                            )
                                        }
                                        Text(
                                            text = favoriteCount.toString(),
                                            style = Regular12,
                                            color = mainGray1,
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
                Box(
                    modifier = Modifier
                        .height(0.5.dp)
                        .fillMaxWidth()
                        .background(mainGray5)
                )
            }
        }
    }
}

@Composable
fun MapSheetContentDeltail(
    popup: PopupEvent,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            horizontalArrangement = SpaceBetween
        ) {
            popup.recommendList.forEach { recommend ->
                Text(
                    text = recommend,
                    style = Regular12,
                    color = Color(0xFF666666)
                )
            }
            IconButton(
                onClick = { onClose() },
                modifier = Modifier
                    .size(17.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0F2F4), RoundedCornerShape(20.dp))
                        .size(17.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.close_icon),
                        contentDescription = "닫기",
                        modifier = Modifier
                            .size(11.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
        Text(
            text = popup.name,
            style = Bold18,
            color = mainBlack,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Row() {
            Text(text = "운영 장소", style = Light12, color = mainGray1)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = popup.roadAddress, style = Regular12, color = mainBlack)
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row() {
            Text(text = "운영 날짜", style = Light12, color = mainGray1)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = popup.startDateFormatted + " - " + popup.endDateFormatted,
                style = Regular12.copy(letterSpacing = (-1).sp),
                color = mainBlack
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        if (!popup.openTime.isNullOrEmpty() && !popup.closeTime.isNullOrEmpty()) {
            Row() {
                Text(text = "운영 시간", style = Light12, color = mainGray1)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = popup.openTime + " ~ " + popup.closeTime,
                    style = Regular12,
                    color = mainBlack
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(popup.fullImageUrlList.getOrNull(0))
                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                .build(),
            contentDescription = popup.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(mainGray4, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun MapSearchBar(
    mapViewModel: MapViewModel,
    isSearched: MutableState<Boolean>,
    loginResponse: LoginResponse?,
    regionsViewModel: RegionsViewModel,
    selectedRegion: String,
    onRegionChange: (String, String) -> Unit
) {
    val userUuid = loginResponse?.userUuid.orEmpty()
    val query by mapViewModel.query.collectAsState(initial = "")
    val keyboard = LocalSoftwareKeyboardController.current
    val isTextFieldEnabled = remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 15.dp, vertical = 15.dp)
            .heightIn(max=56.dp)
            .background(Color.White),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = CenterVertically
        ) {
            Spacer(modifier = Modifier.width(14.dp))
            MapLocalFilterButton(
                selectedRegion = selectedRegion,
                onRegionSelected = { regionWithDistrict ->
                    val parts = regionWithDistrict.split(" ")
                    val region = parts.getOrNull(0) ?: "전체"
                    val district = parts.getOrNull(1) ?: "전체"
                    onRegionChange(region, district)
                },
                regionsViewModel = regionsViewModel
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(mainGray3)
                    .padding(horizontal = 10.dp)
            )
            TextField(
                value = query,
                onValueChange = {
                    mapViewModel.setQuery(it)
                    isTextFieldEnabled.value = true
                    isSearched.value = false
                },
                enabled = isTextFieldEnabled.value,
                placeholder = {
                    Text(
                        text = "궁금한 팝업을 검색해 보세요.",
                        style = Regular12,
                        color = mainGray1
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(3.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = if (isTextFieldEnabled.value) mainBlack else Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Black,
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        mapViewModel.search(userUuid, query)
                        isSearched.value = true
                        mapViewModel.setQuery("")
                        keyboard?.hide()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.serch_icon),
                            contentDescription = "search",
                            modifier = Modifier
                                .size(13.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        mapViewModel.search(userUuid, query)
                        isSearched.value = true
                        mapViewModel.setQuery("")
                        keyboard?.hide()
                    }
                )
            )
        }
    }
}

@Composable
fun MapLocalFilterButton(selectedRegion: String, onRegionSelected: (String) -> Unit, regionsViewModel: RegionsViewModel) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentRegion by remember { mutableStateOf(selectedRegion) }
    val regionsList by regionsViewModel.regions.collectAsState()
    val baseMap: Map<String, List<String>> = if (regionsList.isNotEmpty()) {
        regionsList.associate { it.region to it.districtList }
    } else {
        emptyMap()
    }
    val regionMap: Map<String, List<String>> = if (baseMap.isEmpty()) {
        mapOf("전체" to listOf("전체"))
    } else {
        val ordered = linkedMapOf<String, List<String>>()
        ordered["전체"] = listOf("전체")
        baseMap["서울"]?.let { ordered["서울"] = it }
        for ((k, v) in baseMap) {
            if (k != "서울") ordered.putIfAbsent(k, v)
        }
        ordered
    }
    val regionItems = regionMap.keys.toList()
    val subAreaList = regionMap[currentRegion] ?: listOf("전체")
    val cellHeight = 46.dp
    val tableHeight = (regionItems.size * 46).dp

    Box(
        modifier = Modifier
            .clickable { showSheet = true }
            .background(Color.White)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row {
            Text(
                text = currentRegion ,
                color = mainBlack,
                style = Regular12
            )
            Icon(
                painter = painterResource(R.drawable.arrow_up),
                contentDescription = "지역 선택",
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(10.dp)
                    .rotate(if (showSheet) 270f else 90f),
                tint = mainGray2
            )
        }
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "지역",
                        style = Bold17,
                        color = mainBlack
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.close_icon),
                        contentDescription = "닫기",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showSheet = false },
                        tint = mainBlack
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGray3)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tableHeight)
                        .background(Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .width(65.dp)
                            .background(mainGray4)
                            .height(tableHeight)
                    ) {
                        itemsIndexed(regionItems) { idx, region ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                                    .background(if (currentRegion == region) Color.White else mainGray4)
                                    .clickable { currentRegion = region },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = region,
                                    style = Medium12,
                                    color = if (currentRegion == region) mainOrange else mainGray1
                                )
                            }
                            if (idx != regionItems.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(mainGray4)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(mainGray5)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White)
                            .height(tableHeight)
                    ) {
                        itemsIndexed(subAreaList) { idx, subArea ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                                    .clickable {
                                        onRegionSelected(
                                            if (subArea == "전체") currentRegion else "$currentRegion $subArea"
                                        )
                                        showSheet = false
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = subArea,
                                    style = Medium12,
                                    color = mainBlack
                                )
                            }
                            if (idx != subAreaList.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(mainGray5)
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGray3)
                )
                Spacer(modifier = Modifier.padding(bottom = 20.dp))
            }
        }
    }
}

@Composable
fun MapSortType(selectedSort: String, onSortSelected: (String) -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sortItems = listOf(
        "가까운순" to "CLOSEST",
        "최신순" to "NEWEST",
        "조회순" to "MOST_VIEWED",
        "마감임박순" to "CLOSING_SOON",
    )
    Box(modifier = Modifier
        .clickable { showSheet = true }
        .background(Color.White, RoundedCornerShape(20.dp))
        .border(0.dp, mainGray1, RoundedCornerShape(20.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ){
        Row {
            Text(
                text = sortItems.find { it.second == selectedSort }?.first ?: "가까운순",
                color = mainGray1,
                style = Light10
            )
            Icon(
                painter = painterResource(R.drawable.arrow_up),
                contentDescription = "정렬 선택",
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(10.dp)
                    .rotate(if (showSheet) 270f else 90f),
                tint = mainGray2
            )
        }
    }
    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState, dragHandle = null, containerColor = Color.White) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 28.dp, bottom = 30.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "정렬",
                        style = Bold17,
                        color = mainBlack,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.close_icon),
                        contentDescription = "닫기",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showSheet = false },
                        tint = mainBlack
                    )
                }
                sortItems.forEach { (kor,eng) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortSelected(eng)
                                showSheet = false
                            }
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(if (selectedSort == eng) R.drawable.button_on else R.drawable.button_off),
                                    contentDescription = null,
                                    tint = Color.Unspecified
                                )
                                Text(
                                    text = kor,
                                    style = Regular15,
                                    color = mainBlack,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                )
            }
        }
    }
}







