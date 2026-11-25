package com.poppang.PopPang.rn

import android.os.SystemClock
import android.util.TypedValue
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.SessionStatus
import com.poppang.rn.PopPangRN
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RnSdkIntegrationTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test
    fun sdkBundleAndTypedScreensArePackaged() {
        assertEquals("1.6.0", PopPangRN.VERSION)
        assertTrue(
            "RN screens require an AppCompat application context",
            context.resources
                .newTheme()
                .apply { applyStyle(context.applicationInfo.theme, true) }
                .resolveAttribute(androidx.appcompat.R.attr.colorPrimary, TypedValue(), true),
        )
        context.assets.open("main.android.bundle").use { bundle ->
            assertTrue("SDK JavaScript bundle is empty", bundle.read() >= 0)
        }
        val props = PopPangRN.PopPangScreenProps(userUuid = "contract-check")
        val request = PopPangRN.Screen.PopupRequest(props)
        val management = PopPangRN.Screen.PopupRequestManagement(props)
        assertEquals("popupRequest", request.screenName)
        assertEquals("popupRequestManagement", management.screenName)
        assertEquals("contract-check", request.screenProps().getString("userUuid"))
        assertEquals("contract-check", management.screenProps().getString("userUuid"))
    }

    @Test
    fun entryIntentDoesNotContainAuthenticationTokens() {
        val intent = PopPangRnActivity.createIntent(context, PopPangRnFeature.REQUEST, "contract-check")
        val extras = requireNotNull(intent.extras)
        assertEquals(2, extras.keySet().size)
        assertFalse(extras.keySet().any { it.contains("token", ignoreCase = true) })
        assertEquals(PopPangRnActivity::class.java.name, intent.component?.className)
    }

    /** Requires an unlocked phone and a manually authenticated test account. Never submits a report. */
    @Test
    fun liveRequestLoadsCategoriesAndSystemBackReturnsToHost() =
        runBlocking {
            assumeTrue(InstrumentationRegistry.getArguments().getString("liveRn") == "true")
            AuthTokenStore.initialize(context)
            val manager = AuthTokenStore.session
            assertEquals("Sign in with a test account first", SessionStatus.SIGNED_IN, manager.state.value.status)
            val categories = RetrofitInstance.categoryItemApi.getCategoryItems()
            assertFalse("The server returned no categories", categories.isEmpty())
            val userUuid = requireNotNull(manager.userUuid())
            val intent = PopPangRnActivity.createIntent(context, PopPangRnFeature.REQUEST, userUuid)

            ActivityScenario.launch<PopPangRnActivity>(intent).use { scenario ->
                scenario.onActivity { it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
                val rendered = waitFor { visibleTexts().any { it == "팝업 제보하기" } }
                assertTrue("RN request screen did not render (session=${manager.state.value.status}, activity=${scenario.state})", rendered)
                val categoryNames = categories.map { it.recommendName }.toSet()
                var loaded = false
                repeat(9) {
                    if (!loaded) {
                        loaded = visibleTexts().any { it in categoryNames }
                        if (!loaded) {
                            scrollForward(instrumentation.uiAutomation.rootInActiveWindow)
                            SystemClock.sleep(350)
                        }
                    }
                }
                assertTrue("RN did not display categories obtained with the host Access Token", loaded)
                assertFalse(visibleTexts().any { it.contains("로그인 정보를 확인할 수 없습니다") })
                assertNotNull(manager.requestSession())
                pressBackUnconditionally()
                assertTrue("System back did not close RN", waitFor { scenario.state == Lifecycle.State.DESTROYED })
            }
        }

    private fun visibleTexts(): List<String> {
        val texts = mutableListOf<String>()

        fun visit(node: AccessibilityNodeInfo?) {
            if (node == null) return
            node.text?.toString()?.let(texts::add)
            for (index in 0 until node.childCount) visit(node.getChild(index))
        }
        visit(instrumentation.uiAutomation.rootInActiveWindow)
        return texts
    }

    private fun scrollForward(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isScrollable && node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return true
        return (0 until node.childCount).any { scrollForward(node.getChild(it)) }
    }

    private fun waitFor(check: () -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + 20_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (check()) return true
            SystemClock.sleep(200)
        }
        return check()
    }
}
