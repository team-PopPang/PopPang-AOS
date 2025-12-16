package com.poppang.PopPang.view

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.poppang.PopPang.R
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium18
import com.poppang.PopPang.ui.theme.Regular10
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.ChangeNicknameViewModel
import com.poppang.PopPang.viewmodel.DuplicateNickname
import com.poppang.PopPang.viewmodel.UserDrawViewModel

@Composable
fun ProfileScreen(
    onClose: () -> Unit,
    loginResponse: LoginResponse?,
    DuplicateNickname: DuplicateNickname = viewModel(),
    navController: NavController,
    changeNicknameViewModel: ChangeNicknameViewModel = viewModel(),
    ondataChanged: ((String) -> Unit)? = null
) {
    LaunchedEffect(Unit) {
        DuplicateNickname.onNicknameChange("")
    }
    BackHandler { onClose() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column {
            ProfileTopBar(onClose = onClose)
            ProfileNicknameChange(DuplicateNickname = DuplicateNickname)
            Spacer(modifier = Modifier.height(20.dp))
            ProfileContent(
                loginResponse = loginResponse,
                navController = navController
            )
        }
        CustomButton(
            modifier = Modifier
                .align(BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 20.dp
                ),
            text = "확인",
            onClick = {
                if (DuplicateNickname.isSuccess) {
                    val userUuid = loginResponse?.userUuid.orEmpty()
                    val newNickname = DuplicateNickname.nickname
                    changeNicknameViewModel.changeNickname(userUuid, newNickname)
                    ondataChanged?.invoke(userUuid)
                    onClose()
                }
            },
            backgroundColor = if (DuplicateNickname.isSuccess) mainOrange else mainGray5,
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
fun ProfileContent(
    loginResponse: LoginResponse?,
    userDrawViewModel: UserDrawViewModel = viewModel(),
    navController: NavController
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userUuid = loginResponse?.userUuid.orEmpty()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clickable {
                    prefs.edit().remove("userUuid").apply()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
                .clickable { showWithdrawDialog = true }
        ) {
            Text(
                text = "회원탈퇴",
                style = Regular10,
                color = mainGray2,
            )
        }
    }
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("회원탈퇴") },
            text = { Text("정말로 회원탈퇴 하시겠습니까?") },
            containerColor = Color.White,
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    CustomButton3(
                        onClick = {
                            showWithdrawDialog = false
                            userDrawViewModel.withdrawUser(userUuid)
                            prefs.edit().remove("userUuid").apply()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        text = "탈퇴하기",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    CustomButton4(
                        onClick = { showWithdrawDialog = false },
                        text = "취소",
                        modifier = Modifier
                            .weight(1f)
                            .background(mainOrange, shape = RoundedCornerShape(5.dp))
                    )
                }
            }
        )
    }
}
