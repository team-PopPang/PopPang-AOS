package com.poppang.PopPang.rn

import android.app.Application
import android.os.SystemClock
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.platform.app.InstrumentationRegistry
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.model.AuthResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupStatus
import com.poppang.rn.PopPangRN
import io.nativepiece.host.NativepieceTypedReply
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/** Exercises the production Activity in an isolated app; the test host rejects before any network request. */
class RnSdkSurfaceTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val hostCalls = AtomicInteger()

    @Before
    fun requireIsolatedTestApplication() {
        val context = instrumentation.targetContext
        assumeTrue("Run only in the isolated .rnsdktest app", context.packageName.endsWith(".rnsdktest"))
        AuthTokenStore.initialize(context)
        AuthTokenStore.session.acceptLogin(
            AuthResponse(
                signupStatus = SignupStatus.COMPLETED,
                tokenType = "Bearer",
                user = LoginResponse(userUuid = "sdk-check", provider = "GOOGLE"),
                accessToken = "sdk-test-access",
                refreshToken = "sdk-test-refresh",
                accessTokenExpiresIn = 900,
                refreshTokenExpiresIn = 3600,
                signupToken = null,
                signupTokenExpiresIn = null,
            ),
            "GOOGLE",
        )
        instrumentation.runOnMainSync {
            PopPangRnRuntime.initialize(context.applicationContext as Application)
            PopPangRN.register(
                object : PopPangRN.Host {
                    override fun getAccessToken(
                        request: PopPangRN.GetAccessTokenRequest,
                        reply: NativepieceTypedReply<PopPangRN.GetAccessTokenResponse>,
                    ) {
                        hostCalls.incrementAndGet()
                        reply.reject("temporary_failure", "SDK integration test")
                    }
                },
            )
        }
    }

    @Test
    fun requestScreenSurvivesRecreationAndHeaderBackClosesSurface() {
        open(PopPangRnFeature.REQUEST).use { scenario ->
            assertTrue("Bundled RN request screen did not render", waitFor { containsText("팝업 제보하기") })
            assertTrue("RN did not call the typed host token callback", waitFor { hostCalls.get() > 0 })
            scenario.recreate()
            assertTrue("RN request screen did not survive Activity recreation", waitFor { containsText("팝업 제보하기") })
            assertTrue("RN header back button was not found", clickBack(instrumentation.uiAutomation.rootInActiveWindow))
            assertTrue("RN close event did not reach AOS", waitFor { scenario.state == Lifecycle.State.DESTROYED })
        }
    }

    @Test
    fun managementScreenAsksHostForTokenAndSystemBackClosesSurface() {
        open(PopPangRnFeature.REQUEST_MANAGEMENT).use { scenario ->
            assertTrue("Bundled RN management screen did not render", waitFor { containsText("팝업 제보 관리") })
            assertTrue("RN did not call the typed host token callback", waitFor { hostCalls.get() > 0 })
            pressBackUnconditionally()
            assertTrue("System back did not close RN", waitFor { scenario.state == Lifecycle.State.DESTROYED })
        }
    }

    private fun open(feature: PopPangRnFeature): ActivityScenario<PopPangRnActivity> =
        ActivityScenario
            .launch<PopPangRnActivity>(
                PopPangRnActivity.createIntent(instrumentation.targetContext, feature, "sdk-check"),
            ).also { scenario ->
                scenario.onActivity { it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            }

    private fun containsText(text: String): Boolean {
        fun find(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            if (node.text?.toString() == text) return true
            return (0 until node.childCount).any { find(node.getChild(it)) }
        }
        return find(instrumentation.uiAutomation.rootInActiveWindow)
    }

    private fun clickBack(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.contentDescription?.toString() == "뒤로가기") {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        return (0 until node.childCount).any { clickBack(node.getChild(it)) }
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
