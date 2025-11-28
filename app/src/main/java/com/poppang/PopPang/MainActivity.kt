package com.poppang.PopPang

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets.Type.navigationBars
import android.view.WindowInsets.Type.statusBars
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import com.poppang.PopPang.navigation.Navigation
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import com.poppang.PopPang.viewmodel.AddKeywordViewModel
import com.poppang.PopPang.viewmodel.CategoryItemViewModel
import com.poppang.PopPang.viewmodel.DuplicateNickname
import com.poppang.PopPang.viewmodel.PopupComingViewModel
import com.poppang.PopPang.viewmodel.PopupProgressViewModel
import com.poppang.PopPang.viewmodel.PopupViewModel
import com.poppang.PopPang.viewmodel.RecommendPopupViewModel

class MainActivity : ComponentActivity() {
    private val nicknameViewModel: DuplicateNickname by viewModels()
    private val keywordViewModel: AddKeywordViewModel by viewModels()
    private val categoryViewModel: CategoryItemViewModel by viewModels()
    private val popupprogressViewModel: PopupProgressViewModel by viewModels()
    private val popupcomingViewModel: PopupComingViewModel by viewModels()
    private val recommendPopupViewModel: RecommendPopupViewModel by viewModels()

    private val popupViewModel: PopupViewModel by viewModels()
    private var deepLinkPopupUuid: String? = null
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoSdk.init(this, BuildConfig.KAKAO_KEY)
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        val REQUEST_UPDATE = 100
        deepLinkPopupUuid = intent?.data?.getQueryParameter("popupUuid")
        val listener = object : com.google.android.play.core.install.InstallStateUpdatedListener {
            override fun onStateUpdate(state: com.google.android.play.core.install.InstallState) {
                if (state.installStatus() == com.google.android.play.core.install.model.InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                    appUpdateManager.unregisterListener(this)
                }
            }
        }

        if (!isNetworkAvailable(this)) {
            AlertDialog.Builder(this)
                .setTitle("네트워크 오류")
                .setMessage("인터넷 연결이 필요합니다.\n앱을 종료합니다.")
                .setCancelable(false)
                .setPositiveButton("확인") { _, _ ->
                    finish()
                }
                .show()
            return
        }
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.registerListener(listener)
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    REQUEST_UPDATE
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                }
                requestPermissionLauncher.launch(permission)
            }
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val prefs = getSharedPreferences("fcm", MODE_PRIVATE)
                prefs.edit().putString("fcm_token", token).apply()
            }
        }
        enableEdgeToEdge()
        setContent {
            PopPangAOSTheme {
                var hideSystemBars by remember { mutableStateOf(true) }
                LaunchedEffect(hideSystemBars) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val controller = window.insetsController
                        if (hideSystemBars) {
                            setDecorFitsSystemWindows(window, false)
                            controller?.hide(statusBars() or navigationBars())
                            controller?.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        } else {
                            setDecorFitsSystemWindows(window, true)
                            controller?.show(statusBars() or navigationBars())
                        }
                    } else {
                        setDecorFitsSystemWindows(window, !hideSystemBars)
                    }
                }
                Navigation(
                    nicknameViewModel = nicknameViewModel,
                    keywordViewModel = keywordViewModel,
                    categoryViewModel = categoryViewModel,
                    popupprogressViewModel = popupprogressViewModel,
                    popupcomingViewModel = popupcomingViewModel,
                    popupViewModel = popupViewModel,
                    recommendPopupViewModel = recommendPopupViewModel,
                    hideSystemBars = { hide -> hideSystemBars = hide },
                    deepLinkPopupUuid = deepLinkPopupUuid
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkPopupUuid = intent.data?.getQueryParameter("popupUuid")
    }

    override fun onResume() {
        super.onResume()
        if (!isNetworkAvailable(this)) {
            AlertDialog.Builder(this)
                .setTitle("네트워크 오류")
                .setMessage("인터넷 연결이 필요합니다.\n앱을 종료합니다.")
                .setCancelable(false)
                .setPositiveButton("확인") { _, _ ->
                    finish()
                }
                .show()
        }
    }
}