package com.poppang.PopPang.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout as AndroidLinearLayout
import android.widget.TextView as AndroidTextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

internal sealed interface PopupAdListItem {
    data class Popup(val popup: PopupEvent) : PopupAdListItem
    data object NativeAd : PopupAdListItem
}

internal fun buildPopupAdListItems(popups: List<PopupEvent>): List<PopupAdListItem> {
    val adInsertAfterPopupCounts = POPUP_NATIVE_AD_INSERT_AFTER_POPUP_COUNTS
        .take(popupNativeAdCount(popups.size))

    return buildList {
        popups.forEachIndexed { index, popup ->
            add(PopupAdListItem.Popup(popup))
            if ((index + 1) in adInsertAfterPopupCounts) {
                add(PopupAdListItem.NativeAd)
            }
        }
    }
}

private fun popupNativeAdCount(popupCount: Int): Int =
    POPUP_NATIVE_AD_INSERT_AFTER_POPUP_COUNTS.count { popupCount >= it }

@Composable
internal fun PopupNativeAdCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nativeAdState = remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(context) {
        val adLoader = AdLoader.Builder(context, BuildConfig.ADMOB_NATIVE_KEY)
            .forNativeAd { nativeAd ->
                nativeAdState.value?.destroy()
                nativeAdState.value = nativeAd
            }
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAdState.value?.destroy()
            nativeAdState.value = null
        }
    }

    val nativeAd = nativeAdState.value

    Box(modifier = modifier) {
        if (nativeAd != null) {
            AndroidView(
                factory = { createPopupNativeAdView(it) },
                update = { populatePopupNativeAdView(nativeAd, it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun createPopupNativeAdView(context: Context): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = AndroidLinearLayout.LayoutParams(
            AndroidLinearLayout.LayoutParams.MATCH_PARENT,
            AndroidLinearLayout.LayoutParams.MATCH_PARENT
        )
    }

    val container = AndroidLinearLayout(context).apply {
        orientation = AndroidLinearLayout.VERTICAL
        layoutParams = AndroidLinearLayout.LayoutParams(
            AndroidLinearLayout.LayoutParams.MATCH_PARENT,
            AndroidLinearLayout.LayoutParams.MATCH_PARENT
        )
        background = GradientDrawable().apply {
            setColor(AndroidColor.WHITE)
            cornerRadius = 6.dpToPx(context).toFloat()
            setStroke(1.dpToPx(context), AndroidColor.rgb(229, 229, 229))
        }
    }

    val mediaView = MediaView(context).apply {
        layoutParams = AndroidLinearLayout.LayoutParams(
            AndroidLinearLayout.LayoutParams.MATCH_PARENT,
            165.dpToPx(context)
        )
        setBackgroundColor(AndroidColor.rgb(245, 245, 245))
    }

    val contentContainer = AndroidLinearLayout(context).apply {
        orientation = AndroidLinearLayout.VERTICAL
        setPadding(
            10.dpToPx(context),
            8.dpToPx(context),
            10.dpToPx(context),
            10.dpToPx(context)
        )
    }

    val headlineRow = AndroidLinearLayout(context).apply {
        orientation = AndroidLinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = AndroidLinearLayout.LayoutParams(
            AndroidLinearLayout.LayoutParams.MATCH_PARENT,
            AndroidLinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    val adBadgeView = AndroidTextView(context).apply {
        text = "광고"
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(AndroidColor.WHITE)
        gravity = Gravity.CENTER
        setPadding(4.dpToPx(context), 1.dpToPx(context), 4.dpToPx(context), 1.dpToPx(context))
        background = GradientDrawable().apply {
            setColor(AndroidColor.rgb(255, 137, 37))
            cornerRadius = 3.dpToPx(context).toFloat()
        }
        layoutParams = AndroidLinearLayout.LayoutParams(
            AndroidLinearLayout.LayoutParams.WRAP_CONTENT,
            AndroidLinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            rightMargin = 5.dpToPx(context)
        }
    }

    val headlineView = AndroidTextView(context).apply {
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(AndroidColor.rgb(32, 32, 32))
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        layoutParams = AndroidLinearLayout.LayoutParams(
            0,
            AndroidLinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
    }

    val bodyView = AndroidTextView(context).apply {
        textSize = 11f
        setTextColor(AndroidColor.rgb(110, 110, 110))
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        layoutParams = AndroidLinearLayout.LayoutParams(
            AndroidLinearLayout.LayoutParams.MATCH_PARENT,
            AndroidLinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 4.dpToPx(context)
        }
    }

    val callToActionView = AndroidTextView(context).apply {
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(AndroidColor.WHITE)
        gravity = Gravity.CENTER
        background = GradientDrawable().apply {
            setColor(AndroidColor.rgb(255, 137, 37))
            cornerRadius = 5.dpToPx(context).toFloat()
        }
        layoutParams = AndroidLinearLayout.LayoutParams(
            AndroidLinearLayout.LayoutParams.MATCH_PARENT,
            38.dpToPx(context)
        ).apply {
            topMargin = 8.dpToPx(context)
        }
    }

    headlineRow.addView(adBadgeView)
    headlineRow.addView(headlineView)
    contentContainer.addView(headlineRow)
    contentContainer.addView(bodyView)
    contentContainer.addView(callToActionView)
    container.addView(mediaView)
    container.addView(contentContainer)
    nativeAdView.addView(container)

    nativeAdView.mediaView = mediaView
    nativeAdView.headlineView = headlineView
    nativeAdView.bodyView = bodyView
    nativeAdView.callToActionView = callToActionView

    return nativeAdView
}

private fun populatePopupNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
    (nativeAdView.headlineView as? AndroidTextView)?.text = nativeAd.headline.orEmpty()
    (nativeAdView.bodyView as? AndroidTextView)?.apply {
        val body = nativeAd.body
        visibility = if (body.isNullOrBlank()) View.GONE else View.VISIBLE
        text = body.orEmpty()
    }
    (nativeAdView.callToActionView as? AndroidTextView)?.apply {
        val callToAction = nativeAd.callToAction
        visibility = if (callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
        text = callToAction.orEmpty()
    }
    nativeAd.mediaContent?.let { mediaContent ->
        nativeAdView.mediaView?.mediaContent = mediaContent
    }
    nativeAdView.setNativeAd(nativeAd)
}

private fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).roundToInt()

private val POPUP_NATIVE_AD_INSERT_AFTER_POPUP_COUNTS = listOf(8, 20, 32)
internal const val POPUP_NATIVE_AD_CARD_HEIGHT_DP = 278
