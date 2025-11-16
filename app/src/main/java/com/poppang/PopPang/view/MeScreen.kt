package com.poppang.PopPang.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.poppang.PopPang.R
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Medium18
import com.poppang.PopPang.ui.theme.Regular10
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.AlertViewModel
import com.poppang.PopPang.viewmodel.UserDataViewModel

@Composable
fun MeScreen(
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    showProfile: Boolean = false,
    setShowProfile: (Boolean) -> Unit,
    loginResponse: LoginResponse?,
    navController: NavController,
    userDataViewModel: UserDataViewModel,
    onUpdateLoginResponse: (LoginResponse) -> Unit
) {
    if (showAlarm) {
        AlarmScreen(onClose = { setShowAlarm(false) }, loginResponse = loginResponse)
    }
    else if (showProfile) {
        ProfileScreen(
            onClose = { setShowProfile(false) },
            loginResponse = loginResponse,
            navController = navController,
            ondataChanged = { userUuid: String ->
                userDataViewModel.fetchUserData(userUuid)
            })
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
            MeContent(loginResponse = loginResponse)
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
fun MeContent(loginResponse: LoginResponse?,alertViewModel: AlertViewModel = viewModel()){
    val context = LocalContext.current
    val activity = context as? Activity
    val userUuid = loginResponse?.userUuid.orEmpty()
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentContext by rememberUpdatedState(context)

    LaunchedEffect(Unit) {
        if (!alertViewModel.alertedState && (loginResponse?.isAlerted == true)) {
            alertViewModel.loadAlertStatus(true)
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = ContextCompat.checkSelfPermission(
                    currentContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val alertedState = alertViewModel.alertedState

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
                Box{
                    Switch(
                        checked = alertedState,
                        onCheckedChange = { checked ->
                            if (hasPermission && loginResponse != null) {
                                alertViewModel.AlertStatus(userUuid, checked)
                            }
                        },
                        enabled = hasPermission,
                        modifier = Modifier.scale(0.7f),
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = mainOrange,
                            uncheckedTrackColor = Color.White
                        )
                    )
                    if (!hasPermission && activity != null) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:" + activity.packageName)
                                    }
                                    activity.startActivity(intent)
                                }
                        )
                    }
                }
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