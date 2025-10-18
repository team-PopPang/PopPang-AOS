package com.pappang.poppang_aos.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.pappang.poppang_aos.BuildConfig
import com.pappang.poppang_aos.model.GoogleLoginRequest
import com.pappang.poppang_aos.model.KakaoLoginRequest
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.launch

class loginViewModel : ViewModel() {
    private val authApi = RetrofitInstance.authApi
    private val googlekey = BuildConfig.GOOGLE_KEY
    fun kakaoLogin(
        activity: Activity?,
        context: Context,
        onSuccess: (LoginResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (activity == null) return
        val loginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (token != null) {
                viewModelScope.launch {
                    try {
                        val request = KakaoLoginRequest(access_token = token.accessToken)
                        val response = authApi.kakaoLogin(request)
                        if (response.isSuccessful && response.body() != null) {
                            onSuccess(response.body()!!)
                        } else {
                            onError(Exception("로그인 실패: ${response.errorBody()?.string()}"))
                        }
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            } else if (error != null) {
                onError(error)
            }
        }
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = loginCallback)
                } else {
                    loginCallback(token, error)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = loginCallback)
        }
    }

    fun getGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googlekey)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    fun googleLogin(
        data: Intent?,
        onSuccess: (LoginResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModelScope.launch {
                    try {
                        val request = GoogleLoginRequest(id_token = idToken)
                        val response = authApi.googleLogin(request)
                        if (response.isSuccessful && response.body() != null) {
                            onSuccess(response.body()!!)
                        } else {
                            onError(Exception("Login failed: ${response.errorBody()?.string()}"))
                        }
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            } else {
                onError(Exception("ID Token을 가져올 수 없습니다"))
            }
        } catch (e: ApiException) {
            onError(e)
        }
    }

    fun GoogleLogOut(context: Context, onComplete: () -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googlekey)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun kakaoLogout(onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                onComplete()
            } else {
               onComplete()
            }
        }
    }

    fun saveuserUuid(context: Context, userUuid: String) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("userUuid", userUuid).apply()
   }
}