package com.poppang.PopPang

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.gson.Gson
import com.poppang.PopPang.model.PopupEvent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in decode through the same Coil image loader used by the app's popup screens. */
@RunWith(AndroidJUnit4::class)
class LivePopupImageTest {
    @Test
    fun relativePopupImageLoadsAndDecodesOnAndroid() = runBlocking {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue("Requires -e liveImages true -e imagePath <public image path>", arguments.getString("liveImages") == "true")
        val path = requireNotNull(arguments.getString("imagePath"))
        require(path.startsWith("/images/"))
        val gson = Gson()
        val popup = gson.fromJson(gson.toJson(mapOf("imageUrlList" to listOf(path))), PopupEvent::class.java)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val request = ImageRequest.Builder(context)
            .data(popup.fullImageUrlList.single())
            .size(400)
            .allowHardware(false)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()
        val result = context.imageLoader.execute(request)
        assertTrue("Popup image failed to load: $result", result is SuccessResult)
        val drawable = (result as SuccessResult).drawable
        assertTrue("Image decoded without dimensions", drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0)
    }
}
