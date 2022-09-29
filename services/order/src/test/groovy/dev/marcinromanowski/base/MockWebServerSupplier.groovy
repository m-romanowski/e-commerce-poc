package dev.marcinromanowski.base

import okhttp3.mockwebserver.MockWebServer

interface MockWebServerSupplier {
    MockWebServer getMockWebServer()
}
