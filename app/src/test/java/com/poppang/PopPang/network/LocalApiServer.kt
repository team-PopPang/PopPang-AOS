package com.poppang.PopPang.network

import java.io.Closeable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

internal data class RecordedApiRequest(
    val method: String,
    val path: String,
    val authorization: String?,
    val body: String,
    val contentType: String?,
)

internal data class ApiReply(val status: Int = 200, val body: String = "{}")

/** Real loopback HTTP requests exercise Retrofit serialization and interceptor behavior. */
internal class LocalApiServer : Closeable {
    private val server = MockWebServer()
    private val requests = LinkedBlockingQueue<RecordedApiRequest>()
    @Volatile var respond: (RecordedApiRequest) -> ApiReply = { ApiReply() }
    val url: String get() = server.url("/").toString()

    init {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val recorded = RecordedApiRequest(
                    checkNotNull(request.method),
                    checkNotNull(request.path),
                    request.getHeader("Authorization"),
                    request.body.readUtf8(),
                    request.getHeader("Content-Type"),
                )
                requests.put(recorded)
                val reply = respond(recorded)
                return MockResponse().setResponseCode(reply.status)
                    .setHeader("Content-Type", "application/json").setBody(reply.body)
            }
        }
        server.start()
    }

    fun takeRequest(): RecordedApiRequest = checkNotNull(requests.poll(5, TimeUnit.SECONDS)) { "No HTTP request" }

    override fun close() {
        server.shutdown()
    }
}
