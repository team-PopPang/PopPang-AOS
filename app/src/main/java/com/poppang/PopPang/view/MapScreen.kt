package com.poppang.PopPang.view

import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.naver.maps.geometry.LatLng
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
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainRed
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.MapViewModel
import com.poppang.PopPang.viewmodel.RegionsViewModel
import com.poppang.PopPang.viewmodel.ViewCountViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(
    popupList: List<PopupEvent>,
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    loginResponse: LoginResponse?,
    favoriteViewModel: FavoriteViewModel,
    mapviewmodel: MapViewModel = viewModel(),
    viewCountViewModel: ViewCountViewModel = viewModel(),
    regionsViewModel: RegionsViewModel
) {
    LaunchedEffect(popupList) { mapviewmodel.setPopups(popupList) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false,
        )
    )

    val searchedPopups by mapviewmodel.searchedPopups.collectAsState(initial = emptyList())
    val isSearched = remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContainerColor = Color.White,
        sheetShadowElevation = 8.dp,
        sheetTonalElevation = 8.dp,
        sheetSwipeEnabled = true,
        sheetPeekHeight = (LocalConfiguration.current.screenHeightDp.dp / 3),
        sheetContent = {
            val listToShow = if (isSearched.value && searchedPopups.isNotEmpty()) searchedPopups else popupList
            MapSheetContent(
                popupList = listToShow,
                onPopupClick = { popup ->
                    cameraPositionState.move(
                        CameraUpdate.scrollTo(LatLng(popup.latitude, popup.longitude))
                    )
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                },
                viewCountViewModel = viewCountViewModel,
                favoriteViewModel = favoriteViewModel,
                refreshTrigger = showDetail
            )
        },
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            MapView(
                mapviewmodel = mapviewmodel,
                cameraPositionState = cameraPositionState,
                setShowDetail = { show, popup ->
                    setShowDetail(show)
                    selectedPopup = popup
                }
            )
            MapSearchBar(mapViewModel = mapviewmodel, isSearched = isSearched)
            AnimatedVisibility(
                visible = scaffoldState.bottomSheetState.currentValue != SheetValue.PartiallyExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    colors = buttonColors(containerColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "리스트 아이콘",
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "목록 보기",
                            style = Bold15,
                            color = mainBlack,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

        }
    }
    if (showDetail && selectedPopup != null) {
        ContentDetail(
            popup = selectedPopup!!,
            onClose = { setShowDetail(false) },
            loginResponse = loginResponse,
            favoriteViewModel = favoriteViewModel
        )
    }
}


@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapView(
    mapviewmodel: MapViewModel,
    cameraPositionState: CameraPositionState,
    setShowDetail: (Boolean, PopupEvent?) -> Unit
) {
    val popups by mapviewmodel.popups.collectAsState(initial = emptyList())
    val popupMapById by mapviewmodel.popupMapById.collectAsState(initial = emptyMap())
    val iconSizeDp by mapviewmodel.iconSizeDp.collectAsState(initial = 48f)
    val cornerDp by mapviewmodel.cornerDp.collectAsState(initial = 8f)

    val locationSource = rememberFusedLocationSource()

    var trackingMode by remember { mutableStateOf(LocationTrackingMode.Follow) }
    val uiSettings = remember { MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = true) }

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
    }
}


@Composable
fun MapSheetContent(
    popupList: List<PopupEvent>,
    onPopupClick: (PopupEvent) -> Unit,
    viewCountViewModel: ViewCountViewModel,
    favoriteViewModel: FavoriteViewModel,
    refreshTrigger: Boolean,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = screenHeight * 0.68f)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        LazyColumn {
            items(popupList.size) { index ->
                val popup = popupList[index]
                var favoriteCount by remember { mutableStateOf(0) }
                var viewCount by remember { mutableStateOf(0) }

                LaunchedEffect(popup.popupUuid,refreshTrigger) {
                    favoriteViewModel.getFavoriteCount(popup.popupUuid) { count ->
                        favoriteCount = count.toInt()
                    }
                    viewCountViewModel.getTotalViewCount(popup.popupUuid) { count ->
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
                                            text = popup.roadAddress.split(" ").take(2).joinToString(" "),
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
                                            style = Regular12,
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
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = viewCount.toString(),
                                            style = Regular12,
                                            color = mainGray1,
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.heart_gray_icon),
                                            contentDescription = "좋아요 아이콘",
                                            tint = mainRed,
                                            modifier = Modifier.size(12.dp)
                                        )
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
fun MapSearchBar(mapViewModel: MapViewModel,isSearched: MutableState<Boolean>) {
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
                    mapViewModel.search(query)
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
                    mapViewModel.search(query)
                    isSearched.value = true
                    mapViewModel.setQuery("")
                    keyboard?.hide()
                }
            )
        )
    }
}




