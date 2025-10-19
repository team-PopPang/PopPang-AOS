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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.model.BottomNavItem
import com.pappang.poppang_aos.model.NavIcon
import com.pappang.poppang_aos.ui.theme.Light10

@Composable
fun MainScreen(hideSatausBar: (Boolean) -> Unit = {}) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var showDetail by rememberSaveable { mutableStateOf(false) }
    val items = BottomNavItem.items

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (!showDetail) {
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
                is BottomNavItem.Home -> HomeScreen(hideSatausBar, showDetail = showDetail, setShowDetail = { showDetail = it })
                is BottomNavItem.Calendar -> CalendarScreen()
                is BottomNavItem.Map -> MapScreen()
                is BottomNavItem.PopPang -> LikeScreen()
                is BottomNavItem.My -> MeScreen()
            }
        }
    }
}

@Composable
@Preview
fun MainScreenPreview() {
    MainScreen()
}
