package com.poppang.PopPang

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.poppang.PopPang.view.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun logintest() {
        composeRule.setContent {
            LoginScreen { _, _ -> }
        }

        composeRule.onNodeWithText("카카오 로그인").assertIsDisplayed()
        composeRule.onNodeWithText("구글 로그인").assertIsDisplayed()
    }
}