package com.poppang.PopPang.model

import com.google.gson.Gson
import com.poppang.PopPang.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class PopupImageUrlTest {
    @Test
    fun relativeImagePathsUseOneSlashWithEitherBaseUrlFormat() {
        for (base in listOf("https://poppang.co.kr", "https://poppang.co.kr/")) {
            for (path in listOf("images/18023043641927267/2.jpg", "/images/18023043641927267/2.jpg")) {
                assertEquals(
                    "https://poppang.co.kr/images/18023043641927267/2.jpg",
                    popupImageUrl(base, path),
                )
            }
        }
    }

    @Test
    fun absoluteImageUrlsKeepTheirHostAndQuery() {
        for (url in listOf("https://cdn.example.com/popup.jpg?v=2", "http://cdn.example.com/popup.jpg")) {
            assertEquals(url, popupImageUrl("https://poppang.co.kr/", url))
        }
    }

    @Test
    fun koreanFileNamesAndExistingEncodingArePreserved() {
        assertEquals(
            "https://poppang.co.kr/images/팝업_스토어%201.jpg",
            popupImageUrl("https://poppang.co.kr/", "/images/팝업_스토어%201.jpg"),
        )
    }

    @Test
    fun popupResponseResolvesRelativeImagesAndKeepsImageOrder() {
        val popup = Gson().fromJson(
            """{"imageUrlList":["/images/first.jpg","https://cdn.example.com/second.jpg"]}""",
            PopupEvent::class.java,
        )
        assertEquals(
            listOf(BuildConfig.URL_IMAGE.trimEnd('/') + "/images/first.jpg", "https://cdn.example.com/second.jpg"),
            popup.fullImageUrlList,
        )
        assertEquals(listOf("/images/first.jpg", "https://cdn.example.com/second.jpg"), popup.imageUrlList)
    }
}
