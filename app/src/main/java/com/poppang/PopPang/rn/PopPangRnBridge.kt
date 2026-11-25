package com.poppang.PopPang.rn

import android.app.Application
import com.poppang.PopPang.R
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.network.RnAccessTokenProvider
import com.poppang.PopPang.network.RnAuthException
import com.poppang.rn.PopPangRN
import io.nativepiece.host.NativepieceTypedReply
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** The SDK and host callbacks live for the process, independently of individual RN screens. */
internal object PopPangRnRuntime {
    private var initialized = false

    @Synchronized
    fun initialize(application: Application) {
        if (initialized) return
        application.setTheme(R.style.Theme_PopPangReactNative)
        AuthTokenStore.initialize(application)
        PopPangRN.start(
            application,
            PopPangRnBridge(RnAccessTokenProvider { AuthTokenStore.session }),
            prewarm = false,
        )
        initialized = true
    }
}

internal class PopPangRnBridge(
    private val tokens: RnAccessTokenProvider,
) : PopPangRN.Host {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAccessToken(
        request: PopPangRN.GetAccessTokenRequest,
        reply: NativepieceTypedReply<PopPangRN.GetAccessTokenResponse>,
    ) {
        scope.launch {
            try {
                val token = tokens.getAccessToken(request.rejectedAccessToken, request.expired)
                reply.resolve(PopPangRN.GetAccessTokenResponse(accessToken = token))
            } catch (error: RnAuthException) {
                reply.reject(error.code, error.message ?: "로그인 정보를 확인하지 못했습니다.")
            } catch (_: Exception) {
                reply.reject("temporary_failure", "로그인 정보를 잠시 확인하지 못했습니다.")
            }
        }
    }
}
