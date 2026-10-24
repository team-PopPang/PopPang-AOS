package com.poppang.PopPang.location

import java.util.concurrent.TimeUnit

internal data class MapLocationFix(
    val latitude: Double,
    val longitude: Double,
    val elapsedRealtimeNanos: Long,
)

/** Keeps a late cache result from replacing a newer fix or moving a map the user has dragged. */
internal class MapLocationState(
    private val maxAgeNanos: Long = TimeUnit.MINUTES.toNanos(1),
) {
    var latestFix: MapLocationFix? = null
        private set
    var isCenterPending: Boolean = true
        private set

    fun update(fix: MapLocationFix, nowNanos: Long): Boolean {
        if (!isFresh(fix, nowNanos) || fix.elapsedRealtimeNanos < (latestFix?.elapsedRealtimeNanos ?: 0)) return false
        latestFix = fix
        return true
    }

    fun requestCenter() {
        isCenterPending = true
    }

    fun cancelPendingCenter() {
        isCenterPending = false
    }

    fun consumeCenter(nowNanos: Long): MapLocationFix? {
        if (!isCenterPending) return null
        val fix = latestFix?.takeIf { isFresh(it, nowNanos) } ?: return null
        isCenterPending = false
        return fix
    }

    private fun isFresh(fix: MapLocationFix, nowNanos: Long): Boolean =
        fix.latitude.isFinite() && fix.latitude in -90.0..90.0 &&
            fix.longitude.isFinite() && fix.longitude in -180.0..180.0 &&
            nowNanos - fix.elapsedRealtimeNanos in 0..maxAgeNanos
}
