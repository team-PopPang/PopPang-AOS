package com.poppang.PopPang.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.model.GoogleLoginRequest
import com.poppang.PopPang.model.KakaoLoginRequest
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.apiErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class loginViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    fun kakaoLogin(
        activity: Activity?,
        context: Context,
        onSuccess: (LoginResponse) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        if (activity == null || isLoading) return
        isLoading = true
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (token != null) {
                exchange("KAKAO", token.accessToken, onSuccess, onError)
            } else {
                isLoading = false
                onError(error ?: IllegalStateException("카카오 인증 정보를 가져오지 못했습니다."))
            }
        }
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                else callback(token, null)
            }
        } else UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
    }

    fun getGoogleSignInIntent(context: Context): Intent = GoogleSignIn.getClient(context, googleOptions()).signInIntent

    fun googleLogin(
        context: Context,
        data: Intent?,
        onSuccess: (LoginResponse) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        if (isLoading) return
        isLoading = true
        try {
            val idToken = GoogleSignIn.getSignedInAccountFromIntent(data).result?.idToken
                ?: error("구글 인증 정보를 가져오지 못했습니다.")
            exchange("GOOGLE", idToken, onSuccess, onError)
        } catch (error: Exception) {
            isLoading = false
            onError(error)
        }
    }

    private fun exchange(
        provider: String,
        credential: String,
        onSuccess: (LoginResponse) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val response = if (provider == "KAKAO") {
                    RetrofitInstance.authApi.kakaoLogin(KakaoLoginRequest(credential))
                } else RetrofitInstance.authApi.googleLogin(GoogleLoginRequest(credential))
                check(response.isSuccessful) { apiErrorMessage(response, "로그인에 실패했습니다.") }
                onSuccess(AuthTokenStore.session.acceptLogin(checkNotNull(response.body()).requireData(), provider))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onError(error)
            } finally {
                isLoading = false
            }
        }
    }

    fun GoogleLogOut(context: Context, onComplete: () -> Unit) {
        GoogleSignIn.getClient(context, googleOptions()).signOut().addOnCompleteListener { onComplete() }
    }

    fun kakaoLogout(onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        UserApiClient.instance.logout { onComplete() }
    }

    private fun googleOptions() = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_KEY).requestEmail().build()
}
