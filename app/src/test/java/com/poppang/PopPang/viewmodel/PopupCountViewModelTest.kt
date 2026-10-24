package com.poppang.PopPang.viewmodel

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.FavoriteCountResponse
import com.poppang.PopPang.model.FavoriteRequest
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.model.ViewCountResponse
import com.poppang.PopPang.network.FavoriteApi
import com.poppang.PopPang.network.FavoriteCountApi
import com.poppang.PopPang.network.ViewCountApi
import com.poppang.PopPang.network.ViewCountIncrementApi
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PopupCountViewModelTest {
    @Test
    fun listCountsAreAvailableWithoutPerPopupRequestsOrIntegerTruncation() {
        val fixture = CountsFixture()
        val popups = (1..100).map { popup("popup-$it", 3_000_000_000L + it, 8_000_000_000L + it) }

        fixture.favorites.updateCountsFromList(popups)
        fixture.views.updateCountsFromList(popups)

        popups.forEach {
            assertEquals(it.favoriteCount, fixture.favorites.favoriteCounts.value[it.popupUuid])
            assertEquals(it.viewCount, fixture.views.viewCounts.value[it.popupUuid])
        }
        assertTrue(fixture.api.calls.isEmpty())
    }

    @Test
    fun refreshedListUsesServerCountsAndKeepsPopupsOutsideTheFilter() {
        val fixture = CountsFixture()
        val original = listOf(popup("a", 20, 200), popup("b", 30, 300))
        fixture.favorites.updateCountsFromList(original)
        fixture.views.updateCountsFromList(original)

        // Counts can decrease when favorites or an operational boost are removed.
        val filtered = listOf(popup("a", 19, 190))
        fixture.favorites.updateCountsFromList(filtered)
        fixture.views.updateCountsFromList(filtered)

        assertEquals(mapOf("a" to 19L, "b" to 30L), fixture.favorites.favoriteCounts.value)
        assertEquals(mapOf("a" to 190L, "b" to 300L), fixture.views.viewCounts.value)
        assertTrue(fixture.api.calls.isEmpty())
    }

    @Test
    fun detailQueriesPublishCountsToTheListAsWellAsTheirCallbacks() {
        val fixture = CountsFixture()
        fixture.api.favoriteCount = 12
        fixture.api.viewCount = 123
        var favoriteResult: Long? = null
        var viewResult: Long? = null

        fixture.favorites.getFavoriteCount("user", "a") { favoriteResult = it }
        fixture.views.getTotalViewCount("user", "a") { viewResult = it }

        assertEquals(12L, favoriteResult)
        assertEquals(123L, viewResult)
        assertEquals(12L, fixture.favorites.favoriteCounts.value["a"])
        assertEquals(123L, fixture.views.viewCounts.value["a"])
    }

    @Test
    fun failedQueriesKeepListCountsInsteadOfDisplayingZero() {
        val fixture = CountsFixture()
        val popups = listOf(popup("a", 12, 123))
        fixture.favorites.updateCountsFromList(popups)
        fixture.views.updateCountsFromList(popups)
        fixture.api.readFailure = true
        var favoriteResult: Long? = null
        var viewResult: Long? = null

        fixture.favorites.getFavoriteCount("user", "a") { favoriteResult = it }
        fixture.views.getTotalViewCount("user", "a") { viewResult = it }

        assertEquals(12L, favoriteResult)
        assertEquals(123L, viewResult)
        assertEquals(12L, fixture.favorites.favoriteCounts.value["a"])
        assertEquals(123L, fixture.views.viewCounts.value["a"])
    }

    @Test
    fun failedQueriesWithoutCachedCountsDoNotInventZero() {
        val fixture = CountsFixture()
        fixture.api.readFailure = true
        var callbackCalled = false

        fixture.favorites.getFavoriteCount("user", "a") { callbackCalled = true }
        fixture.views.getTotalViewCount("user", "a") { callbackCalled = true }

        assertFalse(callbackCalled)
        assertTrue(fixture.favorites.favoriteCounts.value.isEmpty())
        assertTrue(fixture.views.viewCounts.value.isEmpty())
    }

    @Test
    fun addingFavoriteRefreshesOnlyAfterTheWriteCompletes() {
        assertFavoriteWriteOrder(adding = true)
    }

    @Test
    fun removingFavoriteRefreshesOnlyAfterTheWriteCompletes() {
        assertFavoriteWriteOrder(adding = false)
    }

    private fun assertFavoriteWriteOrder(adding: Boolean) {
        val fixture = CountsFixture()
        fixture.favorites.updateCountsFromList(listOf(popup("a", 10, 100)))
        val writeGate = CompletableDeferred<Unit>()
        fixture.api.writeGate = writeGate
        fixture.api.favoriteCount = if (adding) 11 else 9
        val operation = if (adding) "add" else "delete"

        if (adding) fixture.favorites.addFavorite("user", "a") else fixture.favorites.deleteFavorite("user", "a")

        assertEquals(listOf("$operation:a"), fixture.api.calls)
        assertEquals(10L, fixture.favorites.favoriteCounts.value["a"])
        writeGate.complete(Unit)

        assertEquals(listOf("$operation:a", "favorites", "favorite-count:a"), fixture.api.calls)
        assertEquals(fixture.api.favoriteCount, fixture.favorites.favoriteCounts.value["a"])
    }

    @Test
    fun failedFavoriteWritesDoNotRefreshOrChangeConfirmedCounts() {
        val fixture = CountsFixture()
        fixture.favorites.updateCountsFromList(listOf(popup("a", 10, 100)))
        fixture.api.writeFailure = true

        fixture.favorites.addFavorite("user", "a")
        fixture.favorites.deleteFavorite("user", "a")

        assertEquals(listOf("add:a", "delete:a"), fixture.api.calls)
        assertEquals(10L, fixture.favorites.favoriteCounts.value["a"])
    }

    @Test
    fun failedFavoriteListRefreshPreservesConfirmedHearts() {
        val fixture = CountsFixture()
        fixture.api.favoritePopups = listOf(popup("a", 10, 100, isFavorited = true))
        fixture.favorites.getFavoriteUserCheck("user")
        fixture.api.readFailure = true

        fixture.favorites.getFavoriteUserCheck("user")

        assertEquals(listOf("a"), fixture.favorites.favoritePopupUuids.value)
    }

    @Test
    fun viewRecordRefreshesAfterSuccessWithoutInventingAnImmediateIncrement() {
        val fixture = CountsFixture()
        fixture.views.updateCountsFromList(listOf(popup("a", 10, 100)))
        fixture.api.viewCount = 100 // The server has not flushed the Redis delta yet.
        val writeGate = CompletableDeferred<Unit>()
        fixture.api.writeGate = writeGate

        fixture.views.incrementViewCount("a")

        assertEquals(listOf("view:a"), fixture.api.calls)
        assertEquals(100L, fixture.views.viewCounts.value["a"])
        writeGate.complete(Unit)

        assertEquals(listOf("view:a", "view-count:a"), fixture.api.calls)
        assertEquals(100L, fixture.views.viewCounts.value["a"])
    }

    @Test
    fun failedViewRecordDoesNotChangeTheTotalOrTriggerAnotherRequest() {
        val fixture = CountsFixture()
        fixture.views.updateCountsFromList(listOf(popup("a", 10, 100)))
        fixture.api.writeFailure = true

        fixture.views.incrementViewCount("a")

        assertEquals(listOf("view:a"), fixture.api.calls)
        assertEquals(100L, fixture.views.viewCounts.value["a"])
    }
}

private class CountsFixture {
    val api = FakeCountsApi()
    val favorites = FavoriteViewModel(api, api, Dispatchers.Unconfined)
    val views = ViewCountViewModel(api, api, Dispatchers.Unconfined)
}

private class FakeCountsApi : FavoriteApi, FavoriteCountApi, ViewCountApi, ViewCountIncrementApi {
    val calls = mutableListOf<String>()
    var favoriteCount = 0L
    var viewCount = 0L
    var favoritePopups = emptyList<PopupEvent>()
    var readFailure = false
    var writeFailure = false
    var writeGate: CompletableDeferred<Unit>? = null

    override suspend fun addFavorite(request: FavoriteRequest) = write("add:${request.popupUuid}")

    override suspend fun deleteFavorite(request: FavoriteRequest) = write("delete:${request.popupUuid}")

    override suspend fun incrementViewCount(popupUuid: String) = write("view:$popupUuid")

    private suspend fun write(operation: String) {
        calls += operation
        writeGate?.await()
        if (writeFailure) throw IOException("Write failed")
    }

    override suspend fun getFavoriteUserCheck(): ApiResponse<List<PopupEvent>> {
        read("favorites")
        return ApiResponse(true, 0, null, favoritePopups)
    }

    override suspend fun getFavoriteCheck(popupUuid: String): ApiResponse<FavoriteCountResponse> {
        read("favorite-count:$popupUuid")
        return ApiResponse(true, 0, null, FavoriteCountResponse(favoriteCount))
    }

    override suspend fun getTotalViewCount(popupUuid: String): ViewCountResponse {
        read("view-count:$popupUuid")
        return ViewCountResponse(viewCount)
    }

    private fun read(operation: String) {
        calls += operation
        if (readFailure) throw IOException("Read failed")
    }
}

private fun popup(id: String, favoriteCount: Long, viewCount: Long, isFavorited: Boolean = false) = PopupEvent(
    popupUuid = id,
    name = "팝업",
    startDate = "2026-10-01",
    endDate = "2026-10-31",
    openingTime = null,
    closingTime = null,
    address = "서울",
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
    favoriteCount = favoriteCount,
    viewCount = viewCount,
    isFavorited = isFavorited,
)
