package com.poppang.PopPang.view.renewal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SignupValidationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun categoriesAreRequiredAndEmptyKeywordsCanOnlyBeSkipped() {
        var categories by mutableStateOf(emptyList<String>())
        var keywords by mutableStateOf(emptyList<String>())
        val submissions = mutableListOf<Boolean>()
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    nickname = "설레는수달",
                    onNicknameChange = {},
                    nicknameMessage = "",
                    nicknameValid = true,
                    nicknameError = false,
                    onCheckNickname = {},
                    categories = listOf("디저트"),
                    selectedCategories = categories,
                    onToggleCategory = { categories = if (it in categories) categories - it else categories + it },
                    keywords = keywords,
                    onAddKeyword = { keywords = keywords + it },
                    onRemoveKeyword = { keywords = keywords - it },
                    onSubmit = { submissions.add(it) },
                    onStart = {},
                )
            }
        }
        val next = compose.onNodeWithTag("signup-next")
        next.performClick()
        next.assertIsNotEnabled().performClick()
        compose.onNodeWithText("어떤 팝업을\n좋아하세요?").assertIsDisplayed()
        compose.onNodeWithText("디저트").performClick()
        next.assertIsEnabled()
        compose.onNodeWithText("디저트").performClick()
        next.assertIsNotEnabled()
        compose.onNodeWithText("디저트").performClick()
        next.performClick()
        next.assertIsNotEnabled().performClick()
        compose.onNodeWithText("건너뛰기").assertIsEnabled()
        val input = compose.onNodeWithTag("signup-keyword")
        input.performTextReplacement("   ")
        compose.onNodeWithText("추가").performClick()
        next.assertIsNotEnabled()
        input.performTextReplacement("레고")
        next.assertIsNotEnabled()
        compose.onNodeWithText("추가").performClick()
        next.assertIsEnabled()
        compose.onNodeWithText("레고").performScrollTo().performClick()
        next.assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(emptyList<Boolean>(), submissions) }
        compose.onNodeWithText("건너뛰기").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(listOf(true), submissions)
            assertEquals(listOf("디저트"), categories)
            assertEquals(emptyList<String>(), keywords)
        }
    }

    @Test fun aPopularKeywordEnablesNextAndSubmissionBlocksRepeatedActionsUntilRetry() {
        var keywords by mutableStateOf(emptyList<String>())
        var submitting by mutableStateOf(false)
        val submissions = mutableListOf<Boolean>()
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    nickname = "설레는수달",
                    onNicknameChange = {},
                    nicknameMessage = "",
                    nicknameValid = true,
                    nicknameError = false,
                    onCheckNickname = {},
                    categories = listOf("디저트"),
                    selectedCategories = listOf("디저트"),
                    onToggleCategory = {},
                    keywords = keywords,
                    onAddKeyword = { keywords = keywords + it },
                    onRemoveKeyword = { keywords = keywords - it },
                    popularKeywords = listOf("산리오"),
                    submitting = submitting,
                    onSubmit = {
                        submissions.add(it)
                        submitting = true
                    },
                    onStart = {},
                )
            }
        }
        val next = compose.onNodeWithTag("signup-next")
        next.performClick().performClick()
        next.assertIsNotEnabled()
        compose.onNodeWithText("+ 산리오").performScrollTo().performClick()
        next.assertIsEnabled().performClick()
        next.assertIsNotEnabled().performClick()
        compose
            .onNodeWithText("건너뛰기")
            .performScrollTo()
            .assertIsNotEnabled()
            .performClick()
        compose.runOnIdle {
            assertEquals(listOf(false), submissions)
            assertEquals(listOf("산리오"), keywords)
            submitting = false
        }
        next.assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(listOf(false, false), submissions) }
    }
}
