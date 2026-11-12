package com.poppang.PopPang.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.view.renewal.renewalText

internal enum class NotificationPermissionAction { ENABLE, REQUEST_PERMISSION, EXPLAIN, SETTINGS }

internal fun notificationPermissionAction(
    apiLevel: Int,
    runtimeGranted: Boolean,
    notificationsEnabled: Boolean,
    showRationale: Boolean,
): NotificationPermissionAction =
    when {
        apiLevel >= 33 && !runtimeGranted ->
            if (showRationale) NotificationPermissionAction.EXPLAIN else NotificationPermissionAction.REQUEST_PERMISSION
        notificationsEnabled -> NotificationPermissionAction.ENABLE
        else -> NotificationPermissionAction.SETTINGS
    }

internal data class NotificationOptIn(
    val request: () -> Unit,
    val waiting: Boolean,
)

/** Permission is checked again after each system screen; only an actual grant enables server alerts. */
@Composable
internal fun rememberNotificationOptIn(
    onAllowed: () -> Unit,
    onLater: () -> Unit,
): NotificationOptIn {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val allowed by rememberUpdatedState(onAllowed)
    val later by rememberUpdatedState(onLater)
    var waiting by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf<NotificationPermissionAction?>(null) }

    val settings =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            waiting = false
            if (context.notificationsAllowed()) allowed() else dialog = NotificationPermissionAction.SETTINGS
        }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            waiting = false
            if (context.notificationsAllowed()) {
                allowed()
            } else {
                dialog =
                    if (activity != null &&
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
                    ) {
                        NotificationPermissionAction.EXPLAIN
                    } else {
                        NotificationPermissionAction.SETTINGS
                    }
            }
        }
    val requestPermission: () -> Unit = {
        dialog = null
        waiting = true
        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    val skip: () -> Unit = {
        dialog = null
        later()
    }
    dialog?.let { action ->
        NotificationPermissionDialog(
            settings = action == NotificationPermissionAction.SETTINGS,
            onConfirm = {
                if (action == NotificationPermissionAction.SETTINGS) {
                    dialog = null
                    waiting = true
                    settings.launch(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                    )
                } else {
                    requestPermission()
                }
            },
            onLater = skip,
        )
    }
    return NotificationOptIn(
        request = {
            if (!waiting && dialog == null) {
                val action =
                    notificationPermissionAction(
                        apiLevel = Build.VERSION.SDK_INT,
                        runtimeGranted = context.runtimeNotificationPermissionGranted(),
                        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
                        showRationale =
                            activity != null &&
                                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS),
                    )
                when (action) {
                    NotificationPermissionAction.ENABLE -> allowed()
                    NotificationPermissionAction.REQUEST_PERMISSION -> requestPermission()
                    else -> dialog = action
                }
            }
        },
        waiting = waiting || dialog != null,
    )
}

private fun Context.runtimeNotificationPermissionGranted(): Boolean =
    Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun Context.notificationsAllowed(): Boolean =
    runtimeNotificationPermissionGranted() && NotificationManagerCompat.from(this).areNotificationsEnabled()

@Composable
internal fun NotificationPermissionDialog(
    settings: Boolean,
    onConfirm: () -> Unit,
    onLater: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLater,
        containerColor = Color.White,
        title = { Text("알림을 켜고 시작할까요?", style = renewalText(20, true)) },
        text = {
            Text(
                if (settings) "휴대폰 알림 설정에서 팝팡 알림을 허용해주세요. 돌아오면 알림 설정을 켜드릴게요." else "관심 키워드의 팝업 소식을 받으려면 알림 권한이 필요해요. 권한을 허용해주세요.",
                style = renewalText(14),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) { Text(if (settings) "알림 설정 열기" else "권한 허용", color = mainOrange, style = renewalText(14, true)) }
        },
        dismissButton = { TextButton(onClick = onLater) { Text("나중에", style = renewalText(14)) } },
    )
}
