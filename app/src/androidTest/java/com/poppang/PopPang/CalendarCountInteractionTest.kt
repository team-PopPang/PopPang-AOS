package com.poppang.PopPang

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.FavoriteCountResponse
import com.poppang.PopPang.model.FavoriteRequest
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.model.ViewCountResponse
import com.poppang.PopPang.network.FavoriteApi
import com.poppang.PopPang.network.FavoriteCountApi
import com.poppang.PopPang.network.HomePopupfilterApi
import com.poppang.PopPang.network.ViewCountApi
import com.poppang.PopPang.network.ViewCountIncrementApi
import com.poppang.PopPang.view.CalendarScreen
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.HomePopupfilterViewModel
import com.poppang.PopPang.viewmodel.RegionsViewModel
import com.poppang.PopPang.viewmodel.SelectPopupViewModel
import com.poppang.PopPang.viewmodel.ViewCountViewModel
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Exercises the real calendar with fake APIs; never changes a server account or records real views. */
class CalendarCountInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recycledCardsDisplayListAndUpdatedCountsWithoutExtraRequests() {
        val api = CalendarTestApi()
        val favorites = FavoriteViewModel(api, api)
        val views = ViewCountViewModel(api, api)
        val home = HomePopupfilterViewModel(api)
        val selected = SelectPopupViewModel()
        val regions = RegionsViewModel()
        composeRule.setContent {
            CalendarScreen(
                popupList = api.popups,
                setShowDetail = {},
                setShowAlarm = {},
                loginResponse = null,
                favoriteViewModel = favorites,
                viewCountViewModel = views,
                homePopupfilterViewModel = home,
                selectPopupViewModel = selected,
                regionsViewModel = regions,
            )
        }

        fun scrollToCard(index: Int, favorites: Long, views: Long) {
            // The calendar and date/filter header occupy the first two lazy-list items.
            composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(index + 2)
            composeRule.onNodeWithText("테스트 팝업 $index", useUnmergedTree = true).assertIsDisplayed()
            composeRule.onNodeWithText(favorites.toString(), useUnmergedTree = true).assertIsDisplayed()
            composeRule.onNodeWithText(views.toString(), useUnmergedTree = true).assertIsDisplayed()
        }

        for (index in listOf(0, 18, 39, 5, 0)) {
            scrollToCard(index, 9_000L + index, 10_000L + index)
        }
        assertEquals(1, api.listRequests.get())
        assertEquals(0, api.countRequests.get())
        assertEquals(0, api.writeRequests.get())

        composeRule.runOnIdle {
            favorites.getFavoriteCount("test", "popup-0") {}
            views.getTotalViewCount("test", "popup-0") {}
        }
        scrollToCard(0, 33_333, 44_444)
        scrollToCard(39, 9_039, 10_039)
        scrollToCard(0, 33_333, 44_444)
        assertEquals(2, api.countRequests.get())
        assertEquals(0, api.writeRequests.get())
    }
}

private class CalendarTestApi : HomePopupfilterApi, FavoriteApi, FavoriteCountApi, ViewCountApi, ViewCountIncrementApi {
    val listRequests = AtomicInteger()
    val countRequests = AtomicInteger()
    val writeRequests = AtomicInteger()
    val popups = (0..39).map { index ->
        PopupEvent(
            popupUuid = "popup-$index",
            name = "테스트 팝업 $index",
            startDate = LocalDate.now().minusDays(1).toString(),
            endDate = LocalDate.now().plusDays(1).toString(),
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
            recommendations = null,
            favoriteCount = 9_000L + index,
            viewCount = 10_000L + index,
            isFavorited = false,
        )
    }

    override suspend fun getHomePopupfilter(region: String, district: String, homeSortStandard: String): List<PopupEvent> {
        listRequests.incrementAndGet()
        return popups
    }

    override suspend fun getFavoriteCheck(popupUuid: String): ApiResponse<FavoriteCountResponse> {
        countRequests.incrementAndGet()
        return ApiResponse(true, 0, null, FavoriteCountResponse(33_333))
    }

    override suspend fun getTotalViewCount(popupUuid: String): ViewCountResponse {
        countRequests.incrementAndGet()
        return ViewCountResponse(44_444)
    }

    override suspend fun getFavoriteUserCheck(): ApiResponse<List<PopupEvent>> {
        countRequests.incrementAndGet()
        return ApiResponse(true, 0, null, emptyList())
    }

    override suspend fun addFavorite(request: FavoriteRequest) {
        writeRequests.incrementAndGet()
    }

    override suspend fun deleteFavorite(request: FavoriteRequest) {
        writeRequests.incrementAndGet()
    }

    override suspend fun incrementViewCount(popupUuid: String) {
        writeRequests.incrementAndGet()
    }
}
