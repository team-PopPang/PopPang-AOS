package com.poppang.PopPang.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.GoogleLoginRequest
import com.poppang.PopPang.model.KakaoLoginRequest
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.network.RetrofitInstance
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
                        Log.e("kakaoLogin", "accessToken: ${token.accessToken}")
                        val request = KakaoLoginRequest(access_token = token.accessToken)
                        val response = authApi.kakaoLogin(request)
                        if (response.isSuccessful && response.body() != null) {
                            onSuccess(response.body()!!)
                        } else {
                            onError(Exception("로그인 실패: ${response.errorBody()?.string()}"))
                            Log.e("kakaoLogin", "Login failed: ${response.errorBody()?.string()}, code: ${response.code()}" )
                        }
                    } catch (e: Exception) {
                        onError(e)
                        Log.e("kakaoLogin", "Exception during login: ${e.message} " )
                    }
                }
            } else if (error != null) {
                onError(error)
                Log.e("kakaoLogin", "Kakao login failed: ${error.message} " )
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
            Log.e("googleLogin", "idToken: $idToken")
            if (idToken != null) {
                viewModelScope.launch {
                    try {
                        val request = GoogleLoginRequest(id_token = idToken)
                        val response = authApi.googleLogin(request)
                        if (response.isSuccessful && response.body() != null) {
                            onSuccess(response.body()!!)
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                            onError(Exception("Login failed: $errorMsg"))
                            Log.e("googleLogin", "Login failed: $errorMsg, code: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        onError(e)
                        Log.e("googleLogin", "Exception during login: ${e.message} ")
                    }
                }
            } else {
                onError(Exception("ID Token을 가져올 수 없습니다"))
            }
        } catch (e: ApiException) {
            onError(e)
            Log.e("googleLogin", "Google sign-in failed: ${e.statusCode}")
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