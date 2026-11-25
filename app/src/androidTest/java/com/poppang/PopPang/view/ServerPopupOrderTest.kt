package com.poppang.PopPang.view

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServerPopupOrderTest {
    @get:Rule val compose = createComposeRule()

    private val serverOrder =
        listOf(
            popup("server-first", "서버 첫번째", "2028-02-01"),
            popup("server-second", "서버 두번째", "2027-01-01"),
        )

    @Test
    fun recommendationBannerKeepsServerPriorityAheadOfStartDate() {
        compose.setContent { PopPangAOSTheme(false) { BannerCarousel(recommendpopupList = serverOrder, onShowDetail = {}) } }
        assertServerOrder()
    }

    @Test
    fun upcomingCarouselKeepsServerPriorityAheadOfStartDate() {
        compose.setContent { PopPangAOSTheme(false) { BoxCarousel({}, serverOrder, {}) } }
        assertServerOrder()
    }

    @Test
    fun relatedPopupCarouselKeepsServerPriorityAheadOfStartDate() {
        compose.setContent { PopPangAOSTheme(false) { relatedPopupListSection(serverOrder, {}, onShowAll = {}) } }
        assertServerOrder()
    }

    private fun assertServerOrder() {
        val first = compose.onNodeWithText("서버 첫번째").getUnclippedBoundsInRoot()
        val second = compose.onNodeWithText("서버 두번째").getUnclippedBoundsInRoot()
        assertTrue("Server response order must take precedence over the local start date", first.left < second.left)
    }

    private fun popup(
        id: String,
        name: String,
        startDate: String,
    ) = PopupEvent(
        popupUuid = id,
        name = name,
        startDate = startDate,
        endDate = "2028-03-31",
        openingTime = null,
        closingTime = null,
        address = "서울 성동구",
        roadAddressValue = null,
        region = "서울",
        latitude = null,
        longitude = null,
        instaPostId = null,
        instaPostUrl = null,
        captionSummary = "",
        imageUrlList = emptyList(),
        mediaType = null,
        recommendations = emptyList(),
        favoriteCount = 0,
        viewCount = 0,
        isFavorited = false,
    )
}
