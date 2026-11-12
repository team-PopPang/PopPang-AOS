package com.poppang.PopPang.view.renewal

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import com.poppang.PopPang.view.NotificationPermissionDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SignupNotificationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun theCompletedScreenDisablesDuplicateTapsWhileSavingAndBackDoesNotOptIn() {
        val starting = mutableStateOf(true)
        var enables = 0
        var skips = 0
        var dispatcher: OnBackPressedDispatcher? = null
        compose.setContent {
            dispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "테스트",
                    {},
                    "",
                    true,
                    false,
                    {},
                    emptyList(),
                    emptyList(),
                    {},
                    emptyList(),
                    {},
                    {},
                    completed = true,
                    starting = starting.value,
                    onCompletedBack = { skips++ },
                    onSubmit = {},
                    onStart = { enables++ },
                )
            }
        }
        compose.onNodeWithText("알림 설정 확인 중…").assertIsNotEnabled().performClick()
        compose.runOnIdle {
            assertEquals(0, enables)
            dispatcher!!.onBackPressed()
            assertEquals(0, skips)
            starting.value = false
        }
        compose.onNodeWithText("알림 켜고 시작하기").performClick()
        compose.runOnIdle {
            assertEquals(1, enables)
            dispatcher!!.onBackPressed()
            assertEquals(1, skips)
            assertEquals(1, enables)
        }
    }

    @Test fun aDeniedPermissionCanBeRequestedAgainOrSkipped() {
        var requests = 0
        var skips = 0
        compose.setContent {
            PopPangAOSTheme(false) {
                NotificationPermissionDialog(false, { requests++ }, { skips++ })
            }
        }
        compose.onNodeWithText("권한 허용").performClick()
        compose.runOnIdle { assertEquals(1, requests) }
        compose.onNodeWithText("나중에").performClick()
        compose.runOnIdle { assertEquals(1, skips) }
    }

    @Test fun blockedPermissionExplainsHowToOpenSystemSettings() {
        var opens = 0
        compose.setContent {
            PopPangAOSTheme(false) {
                NotificationPermissionDialog(true, { opens++ }, {})
            }
        }
        compose.onNodeWithText("알림 설정 열기").performClick()
        compose.runOnIdle { assertEquals(1, opens) }
    }
}
