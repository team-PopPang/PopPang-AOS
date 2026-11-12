package com.poppang.PopPang.view.renewal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import com.poppang.PopPang.R
import kotlin.math.ceil
import kotlin.math.max

internal data class RenewalMarkerImage(
    val bitmap: Bitmap,
    val anchor: PointF,
)

/** Shared by the real map and the account-free UI preview. Coordinates retain the ring's centre. */
internal class RenewalMarkerRenderer(
    context: Context,
) {
    private val density = context.resources.displayMetrics.density
    private val typeface = ResourcesCompat.getFont(context, R.font.noto_sans_kr_variable)
    private val labelFont = Typeface.create(typeface, 700, false)
    private val countFont = Typeface.create(typeface, 900, false)
    private val store = BitmapFactory.decodeResource(context.resources, R.drawable.renewal_asset_82d3f)
    private val selectedStore = BitmapFactory.decodeResource(context.resources, R.drawable.renewal_asset_0de0c)

    fun popup(
        name: String,
        selected: Boolean,
        dimmed: Boolean,
    ): RenewalMarkerImage {
        val unit = density * if (selected) 1.35f else 1f
        val text =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = labelFont
                textSize = 11f * unit
                letterSpacing = -.02f
                color = Color.rgb(25, 25, 25)
            }
        val label = TextUtils.ellipsize(name, text, 180f * unit, TextUtils.TruncateAt.END).toString()
        val labelWidth = text.measureText(label) + 16f * unit
        val labelHeight = (13.2f + 6f) * unit
        val padding = 12f * unit
        val width = ceil(max(46f * unit, labelWidth) + padding * 2).toInt()
        val height = ceil(padding * 2 + 50f * unit + labelHeight).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val layer = canvas.saveLayerAlpha(0f, 0f, width.toFloat(), height.toFloat(), if (dimmed) 128 else 255)
        val centreX = width / 2f
        val centreY = padding + 23f * unit
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                setShadowLayer(8f * unit, 0f, 2f * unit, Color.argb(46, 0, 0, 0))
            }
        canvas.drawCircle(centreX, centreY, 23f * unit, paint)
        paint.clearShadowLayer()
        paint.color = if (selected) Color.rgb(240, 68, 56) else Color.rgb(34, 163, 90)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * unit
        canvas.drawCircle(centreX, centreY, 21.5f * unit, paint)
        paint.style = Paint.Style.FILL
        paint.color = if (selected) Color.rgb(247, 213, 155) else Color.rgb(201, 221, 185)
        canvas.drawCircle(centreX, centreY, 17f * unit, paint)
        val half = 8f * unit
        canvas.drawBitmap(
            if (selected) selectedStore else store,
            null,
            RectF(centreX - half, centreY - half, centreX + half, centreY + half),
            null,
        )
        val top = padding + 50f * unit
        val labelRect = RectF(centreX - labelWidth / 2, top, centreX + labelWidth / 2, top + labelHeight)
        paint.color = Color.WHITE
        paint.setShadowLayer(4f * unit, 0f, unit, Color.argb(31, 0, 0, 0))
        canvas.drawRoundRect(labelRect, 8f * unit, 8f * unit, paint)
        val baseline = top + labelHeight / 2 - (text.ascent() + text.descent()) / 2
        canvas.drawText(label, centreX - text.measureText(label) / 2, baseline, text)
        canvas.restoreToCount(layer)
        return RenewalMarkerImage(bitmap, PointF(.5f, centreY / height))
    }

    fun cluster(count: Int): Bitmap {
        val pixels = ceil(68f * density).toInt()
        val bitmap = Bitmap.createBitmap(pixels, pixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centre = pixels / 2f
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                setShadowLayer(8f * density, 0f, 2f * density, Color.argb(51, 0, 0, 0))
            }
        canvas.drawCircle(centre, centre, 22f * density, paint)
        paint.clearShadowLayer()
        paint.color = Color.rgb(163, 23, 43)
        canvas.drawCircle(centre, centre, 19f * density, paint)
        paint.typeface = countFont
        paint.textSize = 15f * density
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(count.toString(), centre, centre - (paint.ascent() + paint.descent()) / 2, paint)
        return bitmap
    }
}
