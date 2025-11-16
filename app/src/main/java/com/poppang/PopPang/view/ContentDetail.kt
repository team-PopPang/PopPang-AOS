package com.poppang.PopPang.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.poppang.PopPang.R
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.ui.theme.Bold20
import com.poppang.PopPang.ui.theme.Medium15
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.Regular15
import com.poppang.PopPang.ui.theme.Regular9
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.ui.theme.mainRed
import com.poppang.PopPang.viewmodel.FavoriteViewModel
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
    viewCountViewModel: ViewCountViewModel = viewModel()
) {

    val favoritePopupUuids by favoriteViewModel.favoritePopupUuids.collectAsState()
    val userUuid = loginResponse?.userUuid.orEmpty()
    val isLiked = favoritePopupUuids.contains(popup.popupUuid)
    val imageList = popup.fullImageUrlList
    val pagerState = rememberPagerState { imageList.size }
    var favoriteCount by remember { mutableStateOf(popup.favoriteCount.toInt()) }
    var viewCount by remember { mutableStateOf(popup.viewCount.toInt()) }
    LaunchedEffect(Unit) {
        viewCountViewModel.incrementViewCount(popup.popupUuid)
    }
    BackHandler {
        onClose()
    }
    LaunchedEffect(popup.popupUuid) {
        favoriteViewModel.getFavoriteCount(popup.popupUuid) { count ->
            favoriteCount = count.toInt()
        }
        viewCountViewModel.getTotalViewCount(popup.popupUuid) { count ->
            viewCount = count.toInt()
        }
    }
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
                    CustomButton3(onClick = {}, text = "친구에게 공유하기", modifier = Modifier.weight(1f))
                    CustomButton4(
                        onClick = {
                        if(userUuid.isNotEmpty()) {
                            if (isLiked) {
                                favoriteViewModel.deleteFavorite(userUuid, popup.popupUuid)
                            } else {
                                favoriteViewModel.addFavorite(userUuid, popup.popupUuid)
                            }
                            favoriteViewModel.getFavoriteCount(popup.popupUuid) { count ->
                                favoriteCount = count.toInt()
                            }
                        } },
                        text = if (isLiked) "찜 취소하기" else "찜하기",
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isLiked) Color(0xFF999999) else mainOrange, shape = RoundedCornerShape(5.dp)),
                    )
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
                IconButton(
                    onClick = { onClose() },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_icon),
                        contentDescription = "뒤로가기",
                        tint = Color.Black
                    )
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
                    .height(450.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                ) { page ->
                    AsyncImage(
                        model =ImageRequest.Builder(LocalContext.current)
                            .data(imageList[page])
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = popup.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp),
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
            }
            Text(
                text = popup.name,
                style = Bold20,
                color = mainBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp)

            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp,)
            ) {
                   Box(
                       modifier = Modifier
                           .fillMaxWidth(),
                       contentAlignment = Alignment.BottomEnd
                   ) {
                       Row(
                           verticalAlignment = CenterVertically
                       ) {
                           Icon(
                               painter = painterResource(id = R.drawable.eye_icon),
                               contentDescription = "조회수 아이콘",
                               tint = mainGray1,
                               modifier = Modifier.size(13.dp)
                           )
                           Text(
                               text = viewCount.toString(),
                               style = Regular9,
                               color = mainGray1,
                           )
                           Spacer(modifier = Modifier.width(5.dp))
                           Icon(
                               painter = painterResource(id = R.drawable.heart_gray_icon),
                               contentDescription = "좋아요 아이콘",
                               tint = mainRed,
                               modifier = Modifier.size(13.dp)
                           )
                           Text(
                               text = favoriteCount.toString(),
                               style = Regular9,
                               color = mainGray1,
                           )
                       }
                   }
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
                    text =popup.captionSummary,
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
            }
        }
    }
}

@Composable
@Preview
fun ContentDetailPreview() {
    val samplePopup = PopupEvent(
        popupUuid = "sample-uuid",
        name = "팝업 스토어 이벤트",
        startDate = "2024-06-01",
        endDate = "2024-06-30",
        openTime = "10:00",
        closeTime = "20:00",
        address = "서울특별시 강남구 테헤란로 123",
        roadAddress = "서울특별시 강남구 테헤란로 123",
        region = "강남구",
        latitude = 37.501234,
        longitude = 127.039567,
        instaPostId = "sampleInstaId",
        instaPostUrl = "https://www.instagram.com/p/sampleInstaId/",
        captionSummary = "이번 여름, 특별한 팝업 스토어에서 만나보세요! 다양한 혜택과 이벤트가 가득합니다.",
        imageUrlList = listOf(
            "https://via.placeholder.com/600x400.png?text=Image+1",
            "https://via.placeholder.com/600x400.png?text=Image+2",
            "https://via.placeholder.com/600x400.png?text=Image+3"
        ),
        mediaType = "image",
        recommend = "많이 추천된 팝업 스토어입니다.",
        favoriteCount = 150.0,
        viewCount = 1200.0,
        isFavorited = false
    )
    ContentDetail(
        popup = samplePopup,
        onClose = {},
        loginResponse = null,
        favoriteViewModel = viewModel(),
        viewCountViewModel = viewModel { ViewCountViewModel() }
    )
}