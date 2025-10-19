package com.pappang.poppang_aos.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.ui.theme.Bold17
import com.pappang.poppang_aos.ui.theme.Medium15
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.lowblack
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray5

@Composable
fun ContentDetail(onClose: () -> Unit,hideSatausBar: (Boolean) -> Unit = {}) {
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
                    CustomButton3(onClick = {}, text = "찜하기", modifier = Modifier.weight(1f))
                    CustomButton4(onClick = {}, text = "친구에게 공유하기", modifier = Modifier.weight(1f))
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
                    .height(379.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg),
                    contentDescription = "세부내용이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = Crop
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
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Text(
                text = "팝마트 <라부부의 수상한 실험실> 팝업스토어",
                style = Bold17,
                color = mainBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)

            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .shadow(
                        elevation = 1.dp,
                        shape = RectangleShape,
                        clip = false,
                        ambientColor = lowblack.copy(alpha = 0.13f),
                        spotColor = lowblack.copy(alpha = 0.13f)
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "운영 장소",
                        style = Regular12,
                        color = mainGray1,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                    Text(
                        text = "서울특별시 강남구 테헤란로 427 위워크타워 B1 팝마트 팝업스토어",
                        style = Regular12,
                        color = mainBlack,
                    )
                }
                Row(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "운영 날짜",
                        style = Regular12,
                        color = mainGray1,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                    Text(
                        text = "2024.06.01 ~ 2024.06.30",
                        style = Regular12,
                        color = mainBlack,
                    )
                }
                Row {
                    Text(
                        text = "운영 시간",
                        style = Regular12,
                        color = mainGray1,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                    Text(
                        text = "10:00 ~ 20:00",
                        style = Regular12,
                        color = mainBlack,
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGray5)
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "팝마트의 인기 시리즈 <라부부의 수상한 실험실>이 팝업스토어로 찾아옵니다! 이번 팝업스토어에서는 라부부 캐릭터 상품을 비롯해 다양한 한정판 아이템과 특별 이벤트가 준비되어 있습니다. 팝업스토어 방문객들에게는 특별한 혜택과 기념품도 제공될 예정이니, 팝마트 팬이라면 절대 놓치지 마세요! 친구들과 함께 즐거운 시간을 보내고, 라부부의 매력에 빠져보세요!",
                    style = Regular12.copy(letterSpacing = 0.5.sp, lineHeight = 16.8.sp),
                    color = mainBlack,
                )
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGray5)
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "SNS / 홈페이지",
                    style = Medium15,
                    color = mainBlack,
                )
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}

@Composable
@Preview
fun ContentDetailPreview() {
    ContentDetail(onClose = {})
}
