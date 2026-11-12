package com.poppang.PopPang.view

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPermissionTest {
    @Test fun runtimePermissionIsRequestedAgainOnAndroid13AndLater() {
        assertEquals(NotificationPermissionAction.REQUEST_PERMISSION, notificationPermissionAction(33, false, false, false))
        assertEquals(NotificationPermissionAction.EXPLAIN, notificationPermissionAction(37, false, false, true))
        assertEquals(NotificationPermissionAction.EXPLAIN, notificationPermissionAction(37, false, true, true))
    }

    @Test fun aGrantedRuntimePermissionDoesNotOverrideDisabledSystemNotifications() {
        assertEquals(NotificationPermissionAction.SETTINGS, notificationPermissionAction(37, true, false, false))
        assertEquals(NotificationPermissionAction.ENABLE, notificationPermissionAction(37, true, true, false))
    }

    @Test fun olderAndroidVersionsUseNotificationSettingsWithoutRequestingAnUnavailablePermission() {
        assertEquals(NotificationPermissionAction.SETTINGS, notificationPermissionAction(28, false, false, true))
        assertEquals(NotificationPermissionAction.ENABLE, notificationPermissionAction(32, false, true, false))
    }
}
