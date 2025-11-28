package hu.sarmin.yt2ig

import hu.sarmin.yt2ig.util.HttpClientProvider
import mockwebserver3.MockWebServer
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class FakeImageStore : ImageStore {
    override fun fetchPresetImage(id: ImageStore.PresetImageId): ByteArray {
        return ByteArray(10)
    }
}

class FakeNavigation {
    val navStack = mutableListOf<AppState>()
    val navStackHistory = mutableListOf<List<AppState>>()

    val navigation: Navigation = Navigation(
        navigateTo = { newState ->
            navStackHistory.add(navStack.toList())
            navStack.add(newState)
        },
        replaceState = { oldState, newState ->
            navStackHistory.add(navStack.toList())
            val index = navStack.indexOf(oldState)
            if (index != -1) {
                navStack[index] = newState
            }
        }
    )

    fun reset() {
        navStack.clear()
        navStackHistory.clear()
    }
}

class MockWebServerHttpClientProvider(private val realHttpClient: OkHttpClient, mockWebServer: MockWebServer) : HttpClientProvider {
    private class MockWebServerInterceptor(private val mockServerUrl: HttpUrl) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url

            val newUrl = mockServerUrl.newBuilder()
                .encodedPath(originalUrl.encodedPath)
                .query(originalUrl.query)
                .build()

            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            return chain.proceed(newRequest)
        }
    }

    private val client = realHttpClient.newBuilder()
        .addInterceptor(MockWebServerInterceptor(mockWebServer.url("/")))
        .build()

    override fun getClient() = client
}

