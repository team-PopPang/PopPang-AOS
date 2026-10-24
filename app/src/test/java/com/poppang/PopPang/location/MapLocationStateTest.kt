package com.poppang.PopPang.location

import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class MapLocationStateTest {
    private val now = TimeUnit.MINUTES.toNanos(5)
    private val first = MapLocationFix(37.5, 127.0, now - TimeUnit.SECONDS.toNanos(1))

    @Test
    fun firstFixCentersImmediatelyEvenWhenNoLocationExistedAtMapCreation() {
        val state = MapLocationState()
        assertNull(state.consumeCenter(now))
        assertTrue(state.isCenterPending)
        assertTrue(state.update(first, now))
        assertEquals(first, state.consumeCenter(now))
        assertNull(state.consumeCenter(now))
    }

    @Test
    fun laterCacheResponseCannotReplaceNewerLiveLocation() {
        val state = MapLocationState()
        val live = first.copy(latitude = 37.6, elapsedRealtimeNanos = now)
        assertTrue(state.update(live, now))
        assertFalse(state.update(first, now))
        assertEquals(live, state.consumeCenter(now))
        assertEquals(live, state.latestFix)
    }

    @Test
    fun tappingBeforeAnyFixKeepsCenterRequestUntilLocationArrives() {
        val state = MapLocationState()
        state.cancelPendingCenter()
        state.requestCenter()
        assertNull(state.consumeCenter(now))
        state.update(first, now)
        assertEquals(first, state.consumeCenter(now))
    }

    @Test
    fun tappingAfterDraggingUsesNewestCoordinateOnEveryTap() {
        val state = MapLocationState()
        state.update(first, now)
        state.consumeCenter(now)
        state.cancelPendingCenter()
        val moved = first.copy(longitude = 127.1, elapsedRealtimeNanos = now)
        state.update(moved, now)
        assertNull(state.consumeCenter(now))
        repeat(2) {
            state.requestCenter()
            assertEquals(moved, state.consumeCenter(now))
        }
    }

    @Test
    fun gpsUpdatesDoNotStealTheCameraAfterInitialCenter() {
        val state = MapLocationState()
        state.update(first, now)
        state.consumeCenter(now)
        state.update(first.copy(latitude = 37.7, elapsedRealtimeNanos = now), now)
        assertNull(state.consumeCenter(now))
    }

    @Test
    fun draggingWhileWaitingCancelsAutomaticCenter() {
        val state = MapLocationState()
        state.cancelPendingCenter()
        state.update(first, now)
        assertNull(state.consumeCenter(now))
        state.requestCenter()
        assertEquals(first, state.consumeCenter(now))
    }

    @Test
    fun expiredCacheWaitsForFreshFixInsteadOfMovingToAnOldPosition() {
        val state = MapLocationState()
        assertFalse(state.update(first.copy(elapsedRealtimeNanos = 0), now))
        assertNull(state.consumeCenter(now))
        state.update(first, now)
        assertNull(state.consumeCenter(now + TimeUnit.MINUTES.toNanos(2)))
        assertTrue(state.isCenterPending)
        val refreshed = first.copy(elapsedRealtimeNanos = now + TimeUnit.MINUTES.toNanos(2))
        state.update(refreshed, refreshed.elapsedRealtimeNanos)
        assertEquals(refreshed, state.consumeCenter(refreshed.elapsedRealtimeNanos))
    }

    @Test
    fun invalidCoordinatesAndFutureFixesAreIgnored() {
        for (invalid in listOf(
            first.copy(latitude = Double.NaN), first.copy(latitude = 91.0),
            first.copy(longitude = 181.0), first.copy(elapsedRealtimeNanos = now + 1),
        )) {
            val state = MapLocationState()
            assertFalse(state.update(invalid, now))
            assertNull(state.latestFix)
            assertTrue(state.isCenterPending)
        }
    }
}
