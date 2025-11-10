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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.ui.theme.Bold20
import com.pappang.poppang_aos.ui.theme.Medium15
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.Regular15
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray5
import com.pappang.poppang_aos.ui.theme.mainOrange
import com.pappang.poppang_aos.viewmodel.FavoriteViewModel
import com.pappang.poppang_aos.viewmodel.ViewCountViewModel

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

    LaunchedEffect(Unit) {
        viewCountViewModel.incrementViewCount(popup.popupUuid)
    }
    BackHandler {
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
                    CustomButton3(onClick = {}, text = "친구에게 공유하기", modifier = Modifier.weight(1f))
                    CustomButton4(
                        onClick = {
                        if(userUuid.isNotEmpty()) {
                            if (isLiked) {
                                favoriteViewModel.deleteFavorite(userUuid, popup.popupUuid)
                            } else {
                                favoriteViewModel.addFavorite(userUuid, popup.popupUuid)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 15.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = CenterVertically
                ) {
                    IconButton(
                        onClick = { onClose() },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
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
