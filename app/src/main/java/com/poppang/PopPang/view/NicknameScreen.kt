package com.poppang.PopPang.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.ExtraBold24
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium15
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.DuplicateNickname


@Composable
fun NicknameScreen(viewModel: DuplicateNickname = viewModel()) {
    val nickname = viewModel.nickname
    val checkResult = viewModel.checkResult
    val isError = viewModel.isError
    val isSuccess = viewModel.isSuccess

    Box(
        modifier = Modifier
            .background(Color(0xFFFFFFFF))
    ) {
        Column {
            Text(
                text = "닉네임을\n설정해주세요.",
                style = ExtraBold24,
                color = mainBlack,
                modifier = Modifier
                    .padding(start = 24.dp, top = 44.dp)
            )
            Text(
                text = "닉네임은 나중에 변경할 수 있습니다.",
                style = Medium15,
                color = mainGray1,
                modifier = Modifier
                    .padding(start = 24.dp, top = 11.dp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = CenterVertically
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { viewModel.onNicknameChange(it) },
                    placeholder = { Text("이름을 입력해주세요.", style = Medium12, color = mainGray2)  },
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
                    onClick = { viewModel.checkNickname() },
                    text = "중복확인",
                    modifier = Modifier
                        .weight(3f)
                        .padding(start = 5.dp,end = 24.dp)
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
    }
}



@Composable
@Preview
fun NicknameScreenPreview() {
    NicknameScreen()
}

