package com.poppang.PopPang.view

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.poppang.PopPang.R
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Medium18
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.SelectPopupViewModel

@Composable
fun ContentScreen(
    onClose: () -> Unit,
    popupList: List<PopupEvent>,
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    loginResponse: LoginResponse?,
    favoriteViewModel : FavoriteViewModel,
    selectPopupViewModel: SelectPopupViewModel,
    text:String,
) {
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    val selectPopupList by selectPopupViewModel.selectpopupList.collectAsState()
    var detailPopup by remember { mutableStateOf<PopupEvent?>(null) }
    var visible by remember { mutableStateOf(false) }
    var shouldClose by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    LaunchedEffect(visible, shouldClose) {
        if (!visible && shouldClose) {
            onClose()
        }
    }
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
    BackHandler {
        visible = false
        shouldClose = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            Column {
                ContentTopBar(onClose = { onClose() }, text = text)
                ContentListItem(
                    onShowDetail = { popup ->
                        selectedPopup = popup
                        setShowDetail(true)
                    },
                    loginResponse = loginResponse,
                    popupList = popupList
                )
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
    }
}

@Composable
fun ContentTopBar(onClose: () -> Unit,text:String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp)
    ) {
        Row(
            verticalAlignment = CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_icon),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(14.dp)
                    .clickable { onClose() }
            )
            Text(
                text = text,
                style = Medium18,
                color = mainBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 31.dp)
                    .align(CenterVertically),
                textAlign = Center
            )
        }
    }
}

@Composable
fun ContentListItem(
    onShowDetail: (PopupEvent) -> Unit,
    loginResponse: LoginResponse?,
    popupList: List<PopupEvent>
) {
    val userUuid = loginResponse?.userUuid.orEmpty()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
    ) {
        items(popupList.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                pair.forEach { popup ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShowDetail(popup)
                                Log.d("LikeListItem", "Popup clicked: ${popup.name}") }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(217.5.dp),
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(popup.fullImageUrlList.getOrNull(0))
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = popup.name,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = popup.roadAddress.split(" ").take(2).joinToString(" "),
                                style = Regular12,
                                color = mainBlack,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                            )
                            Text(
                                text = popup.name,
                                style = Bold15,
                                color = mainBlack,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(top = 5.dp)
                            )
                            Text(
                                text = popup.startDateFormatted + " - " + popup.endDateFormatted,
                                style = Regular12.copy(letterSpacing = (-1).sp),
                                color = mainGray1,
                                modifier = Modifier
                                    .padding(top = 5.dp)
                            )
                        }
                    }
                }
                if (pair.size == 1)
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
            }
        }
    }
}