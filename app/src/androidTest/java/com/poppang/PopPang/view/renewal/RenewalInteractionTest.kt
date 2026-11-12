package com.poppang.PopPang.view.renewal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RenewalInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun signupUsesFigmaInputHeightsAndKeepsSkipBesideTheTitle() {
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "설레는수달",
                    {},
                    "",
                    true,
                    false,
                    {},
                    RenewalCategories,
                    listOf("디저트"),
                    {},
                    emptyList(),
                    {},
                    {},
                    onSubmit = {},
                    onStart = {},
                )
            }
        }
        compose.onNodeWithTag("signup-nickname").assertHeightIsEqualTo(48.dp)
        compose.onNodeWithTag("signup-next").performClick().performClick()
        compose.onNodeWithTag("signup-keyword").assertHeightIsEqualTo(50.dp)
        val skip = compose.onNodeWithText("건너뛰기").getUnclippedBoundsInRoot()
        val input = compose.onNodeWithTag("signup-keyword").getUnclippedBoundsInRoot()
        val ownKeywords = compose.onNodeWithTag("signup-own-keywords").getUnclippedBoundsInRoot()
        assertEquals(38f, (ownKeywords.top - input.bottom).value, 1f)
        assertTrue("Skip belongs above the keyword input in the final Figma", skip.bottom < input.top)
    }

    @Test fun firstSignupStepToolbarAndSystemBackUseTheSameExitAction() {
        var exits = 0
        var submitting by mutableStateOf(false)
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "설레는수달",
                    {},
                    "",
                    false,
                    false,
                    {},
                    RenewalCategories,
                    emptyList(),
                    {},
                    emptyList(),
                    {},
                    {},
                    onExit = { exits++ },
                    submitting = submitting,
                    onSubmit = {},
                    onStart = {},
                )
            }
        }
        compose.onNodeWithContentDescription("뒤로가기").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, exits) }
        Espresso.pressBack()
        compose.runOnIdle {
            assertEquals(2, exits)
            submitting = true
        }
        compose.onNodeWithContentDescription("뒤로가기").assertIsNotEnabled()
        Espresso.pressBack()
        compose.runOnIdle { assertEquals(2, exits) }
    }

    @Test fun serverNicknameRecommendationStillRequiresDuplicateConfirmation() {
        var nickname by mutableStateOf("직접정한수달")
        var checked by mutableStateOf(true)
        var loading by mutableStateOf(false)
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    nickname,
                    {
                        nickname = it
                        checked = false
                    },
                    "",
                    checked,
                    false,
                    { checked = true },
                    RenewalCategories,
                    emptyList(),
                    {},
                    emptyList(),
                    {},
                    {},
                    onRecommendNickname = { loading = true },
                    nicknameRecommending = loading,
                    onSubmit = {},
                    onStart = {},
                )
            }
        }
        compose.onNodeWithTag("nickname-recommend").performClick().assertIsNotEnabled()
        compose.onNodeWithTag("signup-next").assertIsNotEnabled()
        compose.runOnIdle {
            nickname = "설레는수달"
            checked = false
            loading = false
        }
        compose.onNodeWithTag("signup-nickname").assertTextContains("설레는수달")
        compose.onNodeWithTag("signup-next").assertIsNotEnabled()
        compose.onNodeWithText("확인").performClick()
        compose.onNodeWithTag("signup-next").assertIsEnabled()
    }

    @Test fun popularKeywordFailureAndLoadingDoNotBlockManualEntry() {
        var keywords by mutableStateOf(emptyList<String>())
        var error by mutableStateOf<String?>("인기 키워드를 불러오지 못했어요.")
        var loading by mutableStateOf(false)
        var popular by mutableStateOf(emptyList<String>())
        var entered = 0
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "설레는수달",
                    {},
                    "",
                    true,
                    false,
                    {},
                    RenewalCategories,
                    listOf("디저트"),
                    {},
                    keywords,
                    { keywords = keywords + it },
                    {},
                    popularKeywords = popular,
                    popularKeywordsLoading = loading,
                    popularKeywordsError = error,
                    onKeywordStepEntered = { entered++ },
                    onRetryPopularKeywords = {
                        error = null
                        loading = true
                    },
                    onSubmit = {},
                    onStart = {},
                )
            }
        }
        compose.onNodeWithTag("signup-next").performClick().performClick()
        compose.runOnIdle { assertEquals(1, entered) }
        compose.onNodeWithText("인기 키워드").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("popular-keywords-retry").performScrollTo().performClick()
        compose.onNodeWithTag("signup-keyword").performScrollTo().performTextReplacement("레고")
        compose.onNodeWithText("추가").performClick()
        compose.runOnIdle {
            assertEquals(listOf("레고"), keywords)
            loading = false
            popular = listOf("소금빵", "산리오")
        }
        compose.onNodeWithText("+ 산리오").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("레고", "산리오"), keywords) }
        compose.onNodeWithTag("signup-next").assertIsEnabled()
    }

    @Test fun signupKeepsSelectionsOnBackAndEnforcesTenKeywordLimit() {
        var nickname by mutableStateOf("성수수달")
        var checked by mutableStateOf(false)
        var categories by mutableStateOf(emptyList<String>())
        var keywords by mutableStateOf(emptyList<String>())
        var completed by mutableStateOf(false)
        compose.setContent {
            PopPangAOSTheme(useDarkTheme = false) {
                RenewalSignupFlow(
                    nickname,
                    {
                        nickname = it
                        checked = false
                    },
                    "",
                    checked,
                    false,
                    { checked = true },
                    RenewalCategories,
                    categories,
                    { categories = if (it in categories) categories - it else categories + it },
                    keywords,
                    { keywords = keywords + it },
                    { keywords = keywords - it },
                    completed = completed,
                    onSubmit = { completed = true },
                    onStart = {},
                )
            }
        }
        compose.onNodeWithTag("signup-next").assertIsNotEnabled()
        compose.onNodeWithText("확인").performClick()
        compose.onNodeWithTag("signup-next").performClick()
        compose.onNodeWithText("디저트").performClick()
        compose.onNodeWithTag("signup-next").performClick()
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.onNodeWithText("디저트").assertIsSelected()
        compose.onNodeWithTag("signup-next").performClick()
        compose.onNodeWithTag("signup-keyword").performTextReplacement("아직 추가하지 않은 입력")
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.onNodeWithTag("signup-next").performClick()
        compose.onNodeWithTag("signup-keyword").assertTextContains("아직 추가하지 않은 입력")
        repeat(10) { index ->
            compose.onNodeWithTag("signup-keyword").performTextReplacement("키워드$index")
            compose.onNodeWithText("추가").performClick()
        }
        compose.onNodeWithTag("keyword-count").assertTextEquals("10 / 10")
        compose.onNodeWithTag("signup-keyword").performTextReplacement("넘치는 키워드")
        compose.onNodeWithText("추가").performClick()
        compose.runOnIdle { assertEquals(10, keywords.size) }
        compose.onNodeWithTag("signup-next").performClick()
        compose.onNodeWithTag("signup-complete").assertIsDisplayed()
    }

    @Test fun skippedKeywordsAreNotPresentedAsRegisteredOnCompletion() {
        var completed by mutableStateOf(false)
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "성수수달",
                    {},
                    "",
                    true,
                    false,
                    {},
                    RenewalCategories,
                    listOf("디저트"),
                    {},
                    listOf("소금빵"),
                    {},
                    {},
                    completed = completed,
                    onSubmit = { completed = true },
                    onStart = {},
                )
            }
        }
        compose.onNodeWithTag("signup-next").performClick()
        compose.onNodeWithTag("signup-next").performClick()
        compose.onNodeWithText("건너뛰기").performClick()
        compose.onNodeWithTag("signup-complete").assertIsDisplayed()
        compose.onNodeWithText("소금빵", substring = true).assertDoesNotExist()
        compose.onNodeWithText("좋아하는 키워드를 알림 설정에서 추가해보세요").assertExists()
    }

    @Test fun selectedPreviewKeepsFavoriteAndDetailActionsVisible() {
        val today = renewalToday()
        val popup = RenewalPopup("one", "선택한 팝업", listOf("디저트"), "서울", today, today.plusDays(3))
        var favorites by mutableStateOf(emptySet<String>())
        var opened: String? = null
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalMapUi(
                    listOf(popup),
                    favorites,
                    { favorites = setOf(it) },
                    { opened = it },
                    mapContent = { _, _, select ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            TextButton({ select("one") }) { Text("마커 선택") }
                        }
                    },
                )
            }
        }
        compose.onNodeWithText("마커 선택").performClick()
        compose.onNodeWithTag("map-preview").assertIsDisplayed()
        compose.onNode(hasContentDescription("찜하기") and hasAnyAncestor(hasTestTag("map-preview"))).assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(setOf("one"), favorites) }
        compose.onNodeWithText("상세보기").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("one", opened) }
    }

    @Test fun zeroSearchResultsDoNotRestoreTheUnfilteredList() {
        val today = renewalToday()
        val popups = listOf(RenewalPopup("one", "테스트 팝업", listOf("디저트"), "서울", today, today.plusDays(3)))
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalMapUi(popups, emptySet(), {}, {}, mapContent = {
                    _,
                    _,
                    _,
                    ->
                    Box(Modifier.fillMaxSize())
                })
            }
        }
        compose.onNodeWithTag("map-search").performTextInput("일치하지 않는 이름")
        compose.onNodeWithTag("map-result-count").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "0개"))
        compose.onNodeWithText("조건에 맞는 팝업이 없어요").assertIsDisplayed()
        compose.onNodeWithText("테스트 팝업").assertDoesNotExist()
    }

    @Test fun filterDraftOnlyChangesTheMapWhenApplied() {
        val today = renewalToday()
        val popups =
            listOf(
                RenewalPopup("one", "오늘 디저트", listOf("디저트"), "서울", today, today.plusDays(2)),
                RenewalPopup("two", "내일 캐릭터", listOf("캐릭터"), "서울", today.plusDays(1), today.plusDays(4)),
            )
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalMapUi(popups, emptySet(), {}, {}, mapContent = {
                    _,
                    _,
                    _,
                    ->
                    Box(Modifier.fillMaxSize())
                })
            }
        }
        compose.onNodeWithTag("map-filters").performClick()
        compose.onNode(hasText("캐릭터") and hasAnyAncestor(hasTestTag("map-filter-sheet"))).performClick()
        compose.onNodeWithTag("map-filter-apply").performClick()
        compose.onNodeWithTag("map-result-count").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "1개"))
        compose.onNodeWithText("내일 캐릭터").assertIsDisplayed()
        compose.onNodeWithText("오늘 디저트").assertDoesNotExist()
    }
}
