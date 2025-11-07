package com.pappang.poppang_aos.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium18
import com.pappang.poppang_aos.ui.theme.Regular10
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainOrange
import com.pappang.poppang_aos.viewmodel.DuplicateNickname

@Composable
fun ProfileScreen(onClose: () -> Unit, loginResponse: LoginResponse? ,DuplicateNickname: DuplicateNickname = DuplicateNickname()) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column {
            ProfileTopBar(onClose = {})
            ProfileNicknameChange(DuplicateNickname = DuplicateNickname)
            Spacer(modifier = Modifier.height(20.dp))
            ProfileContent()
        }
        CustomButton(
            modifier = Modifier
                .align(BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 20.dp
                ),
            text = "확인",
            onClick = {}
        )
    }
}

@Composable
fun ProfileTopBar(onClose: () -> Unit) {
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
                text = "프로필 설정",
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
fun ProfileNicknameChange(DuplicateNickname: DuplicateNickname) {
    val nickname = DuplicateNickname.nickname
    val checkResult = DuplicateNickname.checkResult
    val isError = DuplicateNickname.isError
    val isSuccess = DuplicateNickname.isSuccess
    Row(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(),
        verticalAlignment = CenterVertically
    ) {
        OutlinedTextField(
            value = nickname,
            onValueChange = { DuplicateNickname.onNicknameChange(it) },
            placeholder = { Text("이름을 입력해주세요.", style = Medium12, color = mainGray2) },
            modifier = Modifier
                .height(52.dp)
                .weight(6f)
                .padding(start = 24.dp),
            isError = isError,
            textStyle = Medium12.copy(color = Color.Black),
            trailingIcon = {
                when {
                    isError -> {
                        Image(
                            painter = painterResource(id = R.drawable.default_icon),
                            contentDescription = "실패"
                        )
                    }

                    isSuccess -> {
                        Image(
                            painter = painterResource(id = R.drawable.check_icon),
                            contentDescription = "성공"
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when {
                    isError -> Color(0xFFDD0000)
                    isSuccess -> Color(0xFF006813)
                    else -> mainOrange
                },
                unfocusedBorderColor = when {
                    isError -> Color(0xFFDD0000)
                    isSuccess -> Color(0xFF006813)
                    else -> mainGray2
                }
            )
        )
        CustomButton2(
            onClick = { DuplicateNickname.checkNickname() },
            text = "중복확인",
            modifier = Modifier
                .weight(3f)
                .padding(start = 5.dp, end = 24.dp)
        )
    }
    if (checkResult.isNotBlank()) {
        Text(
            text = checkResult,
            color = when {
                isError -> Color(0xFFDD0000)
                isSuccess -> Color(0xFF006813)
                else -> Color.Unspecified
            },
            style = Medium12,
            modifier = Modifier.padding(start = 24.dp, top = 8.dp)
        )
    }
}

@Composable
fun ProfileContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = "로그아웃",
                style = Regular12,
                color = mainBlack,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "회원탈퇴",
                style = Regular10,
                color = mainGray2,
            )
        }
    }
}

@Composable
@Preview
fun ProfileScreenPreview() {
    ProfileScreen(onClose = {}, loginResponse = null)
}
