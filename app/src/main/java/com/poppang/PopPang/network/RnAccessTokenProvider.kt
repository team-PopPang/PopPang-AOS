package com.poppang.PopPang.network

class RnAuthException(
    val code: String,
    message: String,
) : Exception(message)

/** Uses the same token rotation and invalidation rules as native API requests. */
class RnAccessTokenProvider(
    private val session: () -> SessionManager,
) {
    fun getAccessToken(
        rejectedAccessToken: String?,
        expired: Boolean,
    ): String {
        val manager = session()
        val request = manager.requestSession() ?: throw loginRequired()
        if (rejectedAccessToken == null || rejectedAccessToken != request.accessToken) {
            return request.accessToken
        }

        if (!expired) {
            manager.invalidate(request)
            val current = manager.requestSession()
            if (current != null && current.generation == request.generation) return current.accessToken
            throw loginRequired()
        }

        val refreshed = manager.refresh(request)
        if (refreshed != null && manager.isCurrent(refreshed)) return refreshed.accessToken
        val current = manager.requestSession()
        if (current == null || current.generation != request.generation) throw loginRequired()
        throw RnAuthException("temporary_failure", "로그인 정보를 잠시 확인하지 못했습니다.")
    }

    private fun loginRequired() = RnAuthException("login_required", "다시 로그인해 주세요.")
}
