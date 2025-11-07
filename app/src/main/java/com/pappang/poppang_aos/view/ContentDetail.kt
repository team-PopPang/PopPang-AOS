package com.pappang.poppang_aos.view

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.ui.theme.Bold20
import com.pappang.poppang_aos.ui.theme.Medium15
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.Regular15
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray5

@Composable
fun ContentDetail(popup: PopupEvent, onClose: () -> Unit, hideSatausBar: (Boolean) -> Unit = {}) {
    val imageList = popup.fullImageUrlList
    val pagerState = rememberPagerState { imageList.size }

    LaunchedEffect(Unit) {
        hideSatausBar(true)
    }
    BackHandler {
        hideSatausBar(false)
        onClose()
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
                    CustomButton4(onClick = {}, text = "친구에게 공유하기", modifier = Modifier.weight(1f))
                    CustomButton3(onClick = {}, text = "찜하기", modifier = Modifier.weight(1f))
                }
            }
        }
    ) { innerPadding ->
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 54.dp)
                        .align(TopStart),
                    verticalAlignment = CenterVertically
                ) {
                    IconButton(onClick = {
                        hideSatausBar(false)
                        onClose()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
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
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp)
            ) {
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
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
@Preview
fun ContentDetailPreview() {
    ContentDetail(onClose = {}, popup = PopupEvent(
        popupUuid = "1",
        name = "팝업 스토어 이름",
        startDate = "2024-01-01",
        endDate = "2024-01-31",
        openTime = "10:00",
        closeTime = "20:00",
        address = "서울특별시 강남구 테헤란로 123",
        roadAddress = "서울특별시 강남구 테헤란로 123",
        region = "강남구",
        latitude = 37.123456,
        longitude = 127.123456,
        instaPostId = "1234567890",
        instaPostUrl = "https://www.instagram.com/p/1234567890/",
        captionSummary = "이곳은 팝업 스토어의 간단한 설명이 들어가는 곳입니다. 다양한 상품과 이벤트가 준비되어 있습니다.",
        imageUrlList = listOf(
            "/images/popup1.jpg",
            "/images/popup2.jpg"
        ),
        mediaType = "image",
        recommend = "친구에게 추천하고 싶은 팝업 스토어입니다.",
        favoriteCount = 150.0,
        viewCount = 2000.0
    ), hideSatausBar = {})
}
