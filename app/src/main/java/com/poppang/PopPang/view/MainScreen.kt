package com.poppang.PopPang.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults.colors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.poppang.PopPang.model.BottomNavItem
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.NavIcon
import com.poppang.PopPang.ui.theme.Light10
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.FcmTokenViewModel
import com.poppang.PopPang.viewmodel.PopupComingViewModel
import com.poppang.PopPang.viewmodel.PopupProgressViewModel
import com.poppang.PopPang.viewmodel.PopupViewModel
import com.poppang.PopPang.viewmodel.RecommendPopupViewModel
import com.poppang.PopPang.viewmodel.RegionsViewModel
import com.poppang.PopPang.viewmodel.UserDataViewModel
@Composable
fun MainScreen(
    popupprogressViewModel: PopupProgressViewModel,
    popupcomingViewModel: PopupComingViewModel,
    popupViewModel: PopupViewModel,
    recommendPopupViewModel: RecommendPopupViewModel,
    loginResponse: LoginResponse? = null,
    fcmToken: String?,
    favoriteViewModel: FavoriteViewModel = viewModel(),
    fcmTokenViewModel: FcmTokenViewModel = viewModel(),
    regionsViewModel: RegionsViewModel = viewModel(),
    navController: NavController,
    userDataViewModel: UserDataViewModel,
    onUpdateLoginResponse: (LoginResponse) -> Unit
) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var showDetail by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showAlarm by rememberSaveable { mutableStateOf(false) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    val items = BottomNavItem.items
    val userUuid = loginResponse?.userUuid.orEmpty()
    val loginfcmToken = loginResponse?.fcmToken
    val popupprogressList by popupprogressViewModel.popupprogressList.collectAsState()
    val popupcomingList by popupcomingViewModel.popupcomingList.collectAsState()
    val recommendpopupList by recommendPopupViewModel.recommedPopupList.collectAsState()
    val popupList by popupViewModel.popupList.collectAsState()
    val hideBottomNav = showDetail || showSearch || showAlarm || showProfile

    LaunchedEffect(Unit) {
        popupprogressViewModel.fetchPopupProgressEventsOnce()
        popupcomingViewModel.fetchPopupComingEventsOnce()
        popupViewModel.fetchPopupEventsOnce()
        regionsViewModel.getRegions()
    }
    LaunchedEffect(userUuid) {
        if (userUuid.isNotEmpty()) {
            favoriteViewModel.getFavoriteUserCheck(userUuid)
            recommendPopupViewModel.fetchrecommendpopupEventsOnce(userUuid)
        }
    }
    LaunchedEffect(userUuid, fcmToken) {
        if (loginfcmToken != fcmToken) {
            if (userUuid.isNotEmpty() && !fcmToken.isNullOrBlank()) {
               fcmTokenViewModel.sendFcmToken(userUuid, fcmToken)
            }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (!hideBottomNav) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        windowInsets = WindowInsets(0)
                    ) {
                        items.forEachIndexed { index, item ->
                            val isSelected = selectedIndex == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedIndex = index },
                                icon = {
                                    val icon =
                                        if (isSelected) item.selectedIcon else item.unselectedIcon
                                    (icon as? NavIcon.ResIcon)?.let {
                                        Icon(
                                            painter = painterResource(it.id),
                                            contentDescription = item.label,
                                            modifier = Modifier.size(26.dp),
                                            tint = Color.Unspecified
                                        )
                                    }
                                },
                                label = { Text(item.label, style = Light10) },
                                colors = colors(
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopStart
        ) {
            when (items[selectedIndex]) {
                is BottomNavItem.Home -> HomeScreen(
                    showDetail = showDetail,
                    setShowDetail = { showDetail = it },
                    showSearch = showSearch,
                    setSearchScreen = { showSearch = it },
                    showAlarm = showAlarm,
                    setShowAlarm = { showAlarm = it },
                    popupprogressList = popupprogressList,
                    popupcomingList = popupcomingList,
                    recommendpopupList = recommendpopupList,
                    loginResponse = loginResponse,
                    favoriteViewModel = favoriteViewModel,
                    regionsViewModel = regionsViewModel
                )
                is BottomNavItem.Calendar -> CalendarScreen(
                    popupList = popupList,
                    showDetail = showDetail,
                    setShowDetail = { showDetail = it },
                    showAlarm = showAlarm,
                    setShowAlarm = { showAlarm = it },
                    loginResponse = loginResponse,
                    favoriteViewModel = favoriteViewModel,
                    regionsViewModel = regionsViewModel
                )
                is BottomNavItem.Map -> MapScreen(
                    popupList = popupList,
                    showDetail = showDetail,
                    setShowDetail = { showDetail = it },
                    showAlarm = showAlarm,
                    setShowAlarm = { showAlarm = it },
                    loginResponse = loginResponse,
                    favoriteViewModel = favoriteViewModel,
                    regionsViewModel = regionsViewModel
                )
                is BottomNavItem.PopPang -> LikeScreen(
                    popupList = popupList,
                    showDetail = showDetail,
                    setShowDetail = { showDetail = it },
                    showAlarm = showAlarm,
                    setShowAlarm = { showAlarm = it },
                    loginResponse = loginResponse,
                    favoriteViewModel = favoriteViewModel
                )
                is BottomNavItem.My -> MeScreen(
                    showDetail = showDetail,
                    setShowDetail = { showDetail = it },
                    showAlarm = showAlarm,
                    setShowAlarm = { showAlarm = it },
                    showProfile = showProfile,
                    setShowProfile = { showProfile = it },
                    loginResponse = loginResponse,
                    navController = navController,
                    userDataViewModel = userDataViewModel,
                    onUpdateLoginResponse = onUpdateLoginResponse,
                    favoriteViewModel = favoriteViewModel
                )
            }
        }
    }
}
