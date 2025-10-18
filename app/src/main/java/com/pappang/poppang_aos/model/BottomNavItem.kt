package com.pappang.poppang_aos.model

import androidx.annotation.DrawableRes
import com.pappang.poppang_aos.R

sealed class BottomNavItem(
    val label: String,
    val selectedIcon: NavIcon,
    val unselectedIcon: NavIcon
) {
    object Home : BottomNavItem(
        label = "홈",
        selectedIcon = NavIcon.ResIcon(R.drawable.home_icon_black),
        unselectedIcon = NavIcon.ResIcon(R.drawable.home_icon_white)
    )
    object Calendar : BottomNavItem(
        label = "캘린더",
        selectedIcon = NavIcon.ResIcon(R.drawable.calender_icon_black),
        unselectedIcon = NavIcon.ResIcon(R.drawable.calender_icon_white)
    )
    object Map : BottomNavItem(
        label = "팝팡지도",
        selectedIcon = NavIcon.ResIcon(R.drawable.map_icon_black),
        unselectedIcon = NavIcon.ResIcon(R.drawable.map_icon_white)
    )
    object PopPang : BottomNavItem(
        label = "팝팡",
        selectedIcon = NavIcon.ResIcon(R.drawable.like_icon_black),
        unselectedIcon = NavIcon.ResIcon(R.drawable.like_icon_white)
    )
    object My : BottomNavItem(
        label = "마이",
        selectedIcon = NavIcon.ResIcon(R.drawable.me_icon_black),
        unselectedIcon = NavIcon.ResIcon(R.drawable.me_icon_white)
    )

    companion object {
        val items = listOf(Home, Calendar, Map, PopPang, My)
    }
}

sealed interface NavIcon {
    data class ResIcon(@DrawableRes val id: Int) : NavIcon
}
