package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.ui.theme.Bold15
import com.pappang.poppang_aos.ui.theme.Medium18
import com.pappang.poppang_aos.ui.theme.Regular10
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainOrange

@Composable
fun MeScreen(
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    showProfile: Boolean = false,
    setShowProfile: (Boolean) -> Unit,
    loginResponse: LoginResponse?
) {
    if (showAlarm) {
        AlarmScreen(onClose = { setShowAlarm(false) }, loginResponse = loginResponse)
    }
    else if (showProfile) {
        ProfileScreen(onClose = { setShowProfile(false) }, loginResponse = loginResponse,)
    }
    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            MeTopBar(onAlarmClick = { setShowAlarm(true) })
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { setShowProfile(true) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = loginResponse?.nickname ?: "null",
                        style = Bold15,
                        color = mainBlack,
                        modifier = Modifier
                    )
                    Icon(
                        painter = painterResource(R.drawable.arrow_up),
                        contentDescription = "profile",
                        modifier = Modifier
                            .size(16.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(color = Color(0xFFF1F1F1))
            )
            MeContent()
        }
    }
}

@Composable
fun MeTopBar(onAlarmClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "마이페이지",
                style = Medium18,
                color = mainBlack,
            )
            IconButton(
                onClick = { onAlarmClick() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.bell_icon),
                    contentDescription = "bell",
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .size(23.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun MeContent(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "키워드 알림",
                        style = Regular12,
                        color = mainBlack,
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                    )
                    Text(
                        text = "키워드의 팝업이 등록되면 알림을 안내해드립니다.",
                        style = Regular10,
                        color = mainBlack,
                    )
                }
                Switch(
                    checked = true,
                    onCheckedChange = { /* TODO: 상태 변경 로직 추가 */ },
                    modifier = Modifier
                        .scale(0.7f),
                    colors = SwitchDefaults.colors(
                        checkedTrackColor =  mainOrange,
                        uncheckedTrackColor = Color.White
                    )
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "공지사항",
                    style = Regular12,
                    color = mainBlack,
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.arrow_up),
                    contentDescription = "profile",
                    modifier = Modifier
                        .size(16.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "문의하기",
                    style = Regular12,
                    color = mainBlack,
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.arrow_up),
                    contentDescription = "profile",
                    modifier = Modifier
                        .size(16.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "서비스 이용약관",
                    style = Regular12,
                    color = mainBlack,
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.arrow_up),
                    contentDescription = "profile",
                    modifier = Modifier
                        .size(16.dp)
                )
            }
        }
    }
}
@Composable
@Preview
fun MeScreenPreview() {
    MeScreen(setShowAlarm = {}, loginResponse = null, setShowProfile = {})
}