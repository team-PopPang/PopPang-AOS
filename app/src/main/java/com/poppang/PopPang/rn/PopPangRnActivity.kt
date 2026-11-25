package com.poppang.PopPang.rn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.allViews
import androidx.lifecycle.lifecycleScope
import com.facebook.react.ReactRootView
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.network.SessionStatus
import com.poppang.rn.PopPangRN
import com.poppang.rn.PopPangRNScreen
import com.swmansion.rnscreens.fragment.restoration.RNScreensFragmentFactory
import kotlinx.coroutines.launch

enum class PopPangRnFeature { REQUEST, REQUEST_MANAGEMENT }

/** Keeps native tokens out of Intent extras and closes the RN surface when its session changes. */
class PopPangRnActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = RNScreensFragmentFactory()
        super.onCreate(savedInstanceState)
        AuthTokenStore.initialize(applicationContext)
        val manager = AuthTokenStore.session
        val session = manager.requestSession()
        val userUuid = intent.getStringExtra(EXTRA_USER_UUID).orEmpty()
        val feature = PopPangRnFeature.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_FEATURE) }
        if (session == null || userUuid.isBlank() || manager.userUuid() != userUuid || feature == null) {
            finish()
            return
        }

        PopPangRnRuntime.initialize(application)
        lifecycleScope.launch {
            manager.state.collect { state ->
                if (state.generation != session.generation || state.status != SessionStatus.SIGNED_IN) finish()
            }
        }
        setContent {
            val screen =
                remember(feature, userUuid) {
                    val props = PopPangRN.PopPangScreenProps(userUuid = userUuid)
                    when (feature) {
                        PopPangRnFeature.REQUEST -> PopPangRN.Screen.PopupRequest(props)
                        PopPangRnFeature.REQUEST_MANAGEMENT -> PopPangRN.Screen.PopupRequestManagement(props)
                    }
                }
            PopPangRNScreen(
                screen = screen,
                modifier = Modifier.fillMaxSize(),
                onClose = { finish() },
                onEvent = { event ->
                    if (event is PopPangRN.Event.PopupRequestSubmitted) {
                        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_SUBMITTED, true))
                        finish()
                    }
                },
            )
        }
    }

    @Deprecated("RN image picker uses the legacy activity result callback.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // The embedded surface uses AppCompatActivity, so ReactActivityDelegate does not relay picker results.
        val surface = window.decorView.allViews.filterIsInstance<ReactRootView>().firstOrNull()
        surface?.currentReactContext?.onActivityResult(this, requestCode, resultCode, data)
    }

    companion object {
        const val EXTRA_SUBMITTED = "com.poppang.PopPang.rn.submitted"
        private const val EXTRA_FEATURE = "com.poppang.PopPang.rn.feature"
        private const val EXTRA_USER_UUID = "com.poppang.PopPang.rn.userUuid"

        fun createIntent(
            context: Context,
            feature: PopPangRnFeature,
            userUuid: String,
        ): Intent =
            Intent(context, PopPangRnActivity::class.java)
                .putExtra(EXTRA_FEATURE, feature.name)
                .putExtra(EXTRA_USER_UUID, userUuid)
    }
}
