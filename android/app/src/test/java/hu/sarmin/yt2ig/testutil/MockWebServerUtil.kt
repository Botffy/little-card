package hu.sarmin.yt2ig.testutil

import hu.sarmin.yt2ig.MockWebServerHttpClientProvider
import hu.sarmin.yt2ig.util.DefaultHttpClientProvider
import mockwebserver3.MockWebServer

fun MockWebServer.withDispatcher(dispatcher: mockwebserver3.Dispatcher, block: (MockWebServer, MockWebServerHttpClientProvider) -> Unit) {
    this.use {
        this.dispatcher = dispatcher
        this.start()

        val clientProvider = MockWebServerHttpClientProvider(
            realHttpClient = DefaultHttpClientProvider().getClient(),
            mockWebServer = this
        )
        block(this, clientProvider)
    }
}
