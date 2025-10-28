package com.pappang.poppang_aos.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.pappang.poppang_aos.model.BottomNavItem
import com.pappang.poppang_aos.model.NavIcon
import com.pappang.poppang_aos.ui.theme.Light10
import com.pappang.poppang_aos.viewmodel.PopupComingViewModel
import com.pappang.poppang_aos.viewmodel.PopupViewModel
import com.pappang.poppang_aos.viewmodel.SearchViewModel

@Composable
fun MainScreen(hideSatausBar: (Boolean) -> Unit = {}, popupViewModel: PopupViewModel,popupcomingViewModel: PopupComingViewModel, searchViewModel: SearchViewModel) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var showDetail by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    val items = BottomNavItem.items
    val popupList by popupViewModel.popupList.collectAsState()
    val popupcomingList by popupcomingViewModel.popupcomingList.collectAsState()
    val hideBottomNav = showDetail || showSearch

    LaunchedEffect(Unit) {
        popupViewModel.fetchPopupEventsOnce()
        popupcomingViewModel.fetchPopupComingEventsOnce()
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
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
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
                    hideSatausBar,
                    searchViewModel = searchViewModel,
                    showDetail = showDetail,
                    setShowDetail = { showDetail = it },
                    showSearch = showSearch,
                    setSearchScreen = { showSearch = it },
                    popupList = popupList,
                    popupcomingList = popupcomingList
                )
                is BottomNavItem.Calendar -> CalendarScreen()
                is BottomNavItem.Map -> MapScreen()
                is BottomNavItem.PopPang -> LikeScreen()
                is BottomNavItem.My -> MeScreen()
            }
        }
    }
}
