package com.poppang.PopPang.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate
import com.poppang.PopPang.R
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.ui.theme.Bold20
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium15
import com.poppang.PopPang.ui.theme.Regular11
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.Regular15
import com.poppang.PopPang.ui.theme.Regular9
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.PopupRelatedViewModel
import com.poppang.PopPang.viewmodel.ViewCountViewModel

fun createInstagramIntent(context: Context, url: String): Intent {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(context.packageManager) != null) {
    intent.setPackage("com.instagram.android")
        if (intent.resolveActivity(context.packageManager) != null) {
    return intent
}
    }
    return Intent(Intent.ACTION_VIEW, uri)
}
@Composable
fun ContentDetail(
    popup: PopupEvent,
    onClose: () -> Unit,
    loginResponse: LoginResponse?,
    favoriteViewModel: FavoriteViewModel,
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    viewCountViewModel: ViewCountViewModel = viewModel(),
    popupRelatedViewModel: PopupRelatedViewModel = viewModel()
) {
    val favoritePopupUuids by favoriteViewModel.favoritePopupUuids.collectAsState()
    val popuprelatedList by popupRelatedViewModel.relatedPopupList.collectAsState()
    val userUuid = loginResponse?.userUuid.orEmpty()
    val imageList = popup.fullImageUrlList
    val pagerState = rememberPagerState { imageList.size }
    var favoriteCount by remember { mutableStateOf(popup.favoriteCount.toInt()) }
    val isLikedFromServer = favoritePopupUuids.contains(popup.popupUuid)
    var localIsLiked by remember { mutableStateOf(isLikedFromServer) }
    var viewCount by remember { mutableStateOf(popup.viewCount.toInt()) }
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var shouldClose by remember { mutableStateOf(false) }
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    var showContentScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }
    LaunchedEffect(visible, shouldClose) {
        if (!visible && shouldClose) {
            onClose()
        }
    }
    LaunchedEffect(Unit) {
        viewCountViewModel.incrementViewCount(popup.popupUuid)
    }
    BackHandler {
        visible = false
        shouldClose = true
    }
    LaunchedEffect(popup.popupUuid) {
        favoriteViewModel.getFavoriteCount(userUuid, popup.popupUuid) { count ->
            favoriteCount = count.toInt()
        }
        viewCountViewModel.getTotalViewCount(userUuid, popup.popupUuid) { count ->
            viewCount = count.toInt()
        }
    }
    LaunchedEffect(isLikedFromServer) {
        localIsLiked = isLikedFromServer
    }
    LaunchedEffect(userUuid, popup.popupUuid) {
        popupRelatedViewModel.fetchRelatedPopups(userUuid, popup.popupUuid)
    }
    if (showContentScreen) {
        ContentScreen(
            onClose = { showContentScreen = false },
            popupList = popuprelatedList,
            showDetail = showDetail,
            setShowDetail = setShowDetail,
            loginResponse = loginResponse,
            favoriteViewModel = favoriteViewModel,
            text = "이 팝업은 어때?"
        )
    }
    else if (selectedPopup == null) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        NavigationBar(
                            containerColor = Color.White,
                            windowInsets = WindowInsets(0),
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                        ) {
                            CustomButton4(
                                onClick = {
                                    val firstImageUrl = popup.fullImageUrlList.firstOrNull()
                                    val defaultTemplate = if (firstImageUrl != null) {
                                        FeedTemplate(
                                            content = Content(
                                                title = popup.name,
                                                description = popup.captionSummary,
                                                imageUrl = popup.fullImageUrlList.firstOrNull(),
                                                link = Link(
                                                    webUrl = "https://poppang.co.kr/store/detail?popupUuid=${popup.popupUuid}",
                                                    mobileWebUrl = "https://poppang.co.kr/store/detail?popupUuid=${popup.popupUuid}",
                                                    androidExecutionParams = mapOf("popupUuid" to popup.popupUuid),
                                                    iosExecutionParams = mapOf("popupUuid" to popup.popupUuid)
                                                )
                                            ),
                                        )
                                    } else {
                                        TextTemplate(
                                            text = popup.captionSummary,
                                            link = Link(
                                                webUrl = "https://poppang.co.kr/store/detail?popupUuid=${popup.popupUuid}",
                                                mobileWebUrl = "https://poppang.co.kr/store/detail?popupUuid=${popup.popupUuid}",
                                                androidExecutionParams = mapOf("popupUuid" to popup.popupUuid),
                                                iosExecutionParams = mapOf("popupUuid" to popup.popupUuid)
                                            ),
                                            buttonTitle = "앱에서 보기"
                                        )
                                    }
                                    if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
                                        ShareClient.instance.shareDefault(
                                            context,
                                            defaultTemplate
                                        ) { sharingResult, error ->
                                            if (error != null) {
                                                Log.e("ContentDetail", "카카오톡 공유 실패", error)
                                            } else if (sharingResult != null) {
                                                context.startActivity(sharingResult.intent)
                                                Log.e("ContentDetail", "카카오톡 공유 성공")
                                            }
                                        }
                                    } else {
                                        val sharingUrl =
                                            WebSharerClient.instance.makeDefaultUrl(defaultTemplate)
                                        val intent = Intent(Intent.ACTION_VIEW, sharingUrl)
                                        context.startActivity(intent)
                                        Log.e("ContentDetail", "카카오톡 설치 안됨, 웹으로 공유 $sharingUrl")
                                    }
                                },
                                text = "친구에게 공유하기",
                                modifier = Modifier.weight(1f)
                                    .background(mainOrange, shape = RoundedCornerShape(5.dp))
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(0.1f)
                            ) {
                                IconButton(
                                    onClick = {
                                        val newLikeStatus = !localIsLiked
                                        localIsLiked = newLikeStatus // 즉시 UI 반영
                                        if (newLikeStatus) {
                                            favoriteViewModel.addFavorite(userUuid, popup.popupUuid)
                                            favoriteCount += 1
                                        } else {
                                            favoriteViewModel.deleteFavorite(
                                                userUuid,
                                                popup.popupUuid
                                            )
                                            favoriteCount = (favoriteCount - 1).coerceAtLeast(0)
                                        }
                                    },
                                    modifier = Modifier.size(25.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (localIsLiked) R.drawable.heart_gray_icon else R.drawable.heart_white_icon),
                                        contentDescription = "좋아요 아이콘",
                                        tint = if (localIsLiked) Color.Red else Color.Unspecified,

                                        )
                                }
                                Text(
                                    text = favoriteCount.toString(),
                                    style = Medium12,
                                    color = mainOrange,
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 15.dp)
                        .zIndex(1f)
                ) {
                    Row(
                        verticalAlignment = CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    visible = false
                                    shouldClose = true
                                },
                                modifier = Modifier.size(14.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_down),
                                    contentDescription = "뒤로가기",
                                    tint = Color.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(379.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                        ) { page ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageList[page])
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = popup.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(379.dp),
                                contentScale = Crop
                            )
                        }
                        ImageIndicator(
                            modifier = Modifier
                                .align(BottomCenter)
                                .padding(bottom = 16.dp),
                            currentPage = { pagerState.currentPage },
                            totalPages = { imageList.size }
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 20.dp, bottom = 20.dp)
                                .background(
                                    Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .align(Alignment.BottomStart)

                        ) {
                            Row(
                                verticalAlignment = CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = viewCount.toString(),
                                    style = Regular9,
                                    color = mainBlack,
                                )
                                Text(
                                    text = "명이 봤어요",
                                    style = Regular9,
                                    color = mainBlack,
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
                    ) {
                        Text(
                            text = popup.name,
                            style = Bold20,
                            color = mainBlack,
                            modifier = Modifier
                                .padding(bottom = 5.dp)
                        )
                        popup.recommendList.forEach { recommend ->
                            RecommendListPlaceholder(text = recommend, modifier = Modifier)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp,)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(mainGray5)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "운영 장소",
                                style = Regular15,
                                color = mainGray1,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                            Text(
                                text = popup.roadAddress,
                                style = Regular15,
                                color = mainBlack,
                            )
                            IconButton(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText(
                                        "주소 복사",
                                        popup.roadAddress
                                    )
                                    clipboard.setPrimaryClip(clip)
                                },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.copy_icon),
                                    contentDescription = "복사 아이콘",
                                    tint = mainGray2,
                                    modifier = Modifier
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "운영 날짜",
                                style = Regular15,
                                color = mainGray1,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                            Text(
                                text = popup.startDateFormatted + " - " + popup.endDateFormatted,
                                style = Regular15.copy(letterSpacing = (-1).sp),
                                color = mainBlack,
                            )
                        }
                        if (!popup.openTime.isNullOrEmpty() && !popup.closeTime.isNullOrEmpty()) {
                            Row {
                                Text(
                                    text = "운영 시간",
                                    style = Regular15,
                                    color = mainGray1,
                                    modifier = Modifier.padding(end = 20.dp)
                                )
                                Text(
                                    text = popup.openTime + " ~ " + popup.closeTime,
                                    style = Regular15,
                                    color = mainBlack,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(mainGray5)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = popup.captionSummary,
                            style = Regular12,
                            color = mainBlack,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(mainGray5)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "SNS / 홈페이지",
                            style = Medium15,
                            color = mainBlack,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            val context = LocalContext.current
                            InstagramButton(
                                onClick = {
                                    val intent = createInstagramIntent(
                                        context,
                                        popup.instaPostUrl
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(mainGray5)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        relatedPopupListSection(
                            popuprelatedList = popuprelatedList,
                            onPopupClick = { popup ->
                                selectedPopup = popup
                            },
                            modifier = Modifier,
                            onShowAll = { showContentScreen = true }
                        )
                    }
                }
            }
        }
    } else {
        ContentDetail(
            popup = selectedPopup!!,
            onClose = {
                selectedPopup = null
            },
            loginResponse = loginResponse,
            favoriteViewModel = favoriteViewModel,
            viewCountViewModel = viewCountViewModel,
            popupRelatedViewModel = popupRelatedViewModel,
            showDetail = showDetail,
            setShowDetail = setShowDetail,
        )
    }
}

@Composable
fun RecommendListPlaceholder(text: String,modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFFFF6ED), shape = RoundedCornerShape(25.dp))
    ){
        Text(
            text = text,
            style = Regular11,
            color = Color(0xFFDE5C00),
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun relatedPopupListSection(
    popuprelatedList: List<PopupEvent>,
    onPopupClick: (PopupEvent) -> Unit,
    modifier: Modifier = Modifier,
    onShowAll: () -> Unit
) {
    val sortedList = popuprelatedList.sortedBy { it.startDate }
    val pagerState = rememberPagerState(initialPage = 0) { sortedList.size}
    if (popuprelatedList.isNotEmpty()) {
        Column(
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = SpaceBetween
            ) {
                Text(
                    text = "이 팝업은 어때?",
                    style = Medium15,
                    color = Color.Black,
                )
                IconButton(
                    onClick = {onShowAll()},
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_up),
                        contentDescription = "Arrow Right",
                        modifier = Modifier
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(115.dp),
                pageSpacing = 15.dp,
                modifier = modifier
            ) {
                page ->
                val popup = sortedList[page]
                Box(
                    modifier = Modifier
                        .size(width = 115.dp, height = 162.dp)
                        .clickable { onPopupClick(popup) }
                ) {
                    Column {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(popup.fullImageUrlList.getOrNull(0))
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = popup.name,
                            modifier = Modifier
                                .height(115.dp),
                            contentScale = Crop
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = popup.roadAddress.split(" ").take(2).joinToString(" "),
                            style = Regular12,
                            color = mainBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = popup.name,
                            style = Medium12,
                            color = mainBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}