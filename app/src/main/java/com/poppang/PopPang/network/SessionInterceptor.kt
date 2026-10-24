package com.poppang.PopPang.network

import com.google.gson.Gson
import com.poppang.PopPang.model.ApiError
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class SessionInterceptor(
    private val session: () -> SessionManager,
    private val signup: Boolean = false,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val manager = session()
        if (signup) {
            val signupSession = manager.signupRequest() ?: return denied(chain, 401, 5002)
            val response = chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer ${signupSession.token}").build())
            if (manager.state.value.generation != signupSession.generation) {
                response.close()
                throw IOException("로그인 상태가 변경되었습니다.")
            }
            if (response.code == 401) manager.cancelSignup()
            return response
        }

        val requestSession = manager.requestSession()
            ?: return if (manager.state.value.status == SessionStatus.SIGNING_UP) {
                denied(chain, 403, 5008)
            } else {
                denied(chain, 401, 5006)
            }
        var response = chain.proceed(
            chain.request().newBuilder().header("Authorization", "Bearer ${requestSession.accessToken}").build(),
        )
        if (!manager.isCurrent(requestSession)) {
            response.close()
            throw IOException("로그인 상태가 변경되었습니다.")
        }
        if (response.code != 401) return response

        if (errorCode(response) != 5002) {
            manager.invalidate(requestSession)
            return response
        }
        val refreshed = manager.refresh(requestSession) ?: return response
        response.close()
        response = chain.proceed(
            chain.request().newBuilder().header("Authorization", "Bearer ${refreshed.accessToken}").build(),
        )
        if (!manager.isCurrent(refreshed)) {
            response.close()
            throw IOException("로그인 상태가 변경되었습니다.")
        }
        if (response.code == 401) manager.invalidate(refreshed)
        return response
    }

    private fun errorCode(response: Response): Int? = try {
        Gson().fromJson(response.peekBody(8192).string(), ApiError::class.java)?.code
    } catch (_: Exception) {
        null
    }

    private fun denied(chain: Interceptor.Chain, http: Int, code: Int): Response = Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(http)
        .message("Authentication required")
        .body("""{"success":false,"code":$code,"message":"다시 로그인해 주세요.","data":null}""".toResponseBody("application/json".toMediaType()))
        .build()
}
