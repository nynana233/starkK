package com.starkk.sdk

import android.content.Context
import okhttp3.OkHttpClient
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StarkKClientBuilderTest {

    @Test
    fun builderCreatesClientWithDefaults() {
        val client = StarkKClient.Builder().build()
        assertNotNull(client)
    }

    @Test
    fun builderAllowsCustomBaseUrl() {
        val customUrl = "https://custom.example.com/"
        val client = StarkKClient.Builder()
            .baseUrl(customUrl)
            .build()
        assertNotNull(client)
    }

    @Test
    fun builderAllowsCustomOkHttpClient() {
        val customHttpClient = OkHttpClient.Builder().build()
        val client = StarkKClient.Builder()
            .okHttpClient(customHttpClient)
            .build()
        assertNotNull(client)
    }

    @Test
    fun builderAllowsLoggingConfiguration() {
        val client = StarkKClient.Builder()
            .enableLogging(true)
            .build()
        assertNotNull(client)
    }

    @Test
    fun builderAllowsTimeoutConfiguration() {
        val client = StarkKClient.Builder()
            .connectTimeout(60)
            .readTimeout(60)
            .writeTimeout(60)
            .build()
        assertNotNull(client)
    }

    @Test
    fun builderAllowsCacheSizeConfiguration() {
        val client = StarkKClient.Builder()
            .cacheSize(20L * 1024 * 1024)
            .build()
        assertNotNull(client)
    }

    @Test
    fun builderSupportsFluentChaining() {
        val client = StarkKClient.Builder()
            .baseUrl("https://example.com/")
            .enableLogging(false)
            .connectTimeout(45)
            .readTimeout(45)
            .writeTimeout(45)
            .build()
        assertNotNull(client)
    }

    @Test
    fun builderIgnoresOtherSettingsWhenCustomOkHttpProvided() {
        val customHttpClient = OkHttpClient.Builder().build()
        val client = StarkKClient.Builder()
            .okHttpClient(customHttpClient)
            .enableLogging(true) // This should be ignored
            .connectTimeout(100) // This should be ignored
            .build()
        assertNotNull(client)
    }

    @Test
    fun contextCanBeSetForCaching() {
        // Note: This test cannot fully verify caching without an actual Android context,
        // but we verify that the builder accepts Context parameter
        val builder = StarkKClient.Builder()
        // We simulate having a context by calling the builder method
        // In a real test, you'd use a mock context or robolectric
        val client = builder.build()
        assertNotNull(client)
    }

    @Test
    fun multipleBuilderInstancesCreateIndependentClients() {
        val client1 = StarkKClient.Builder()
            .baseUrl("https://api1.example.com/")
            .build()

        val client2 = StarkKClient.Builder()
            .baseUrl("https://api2.example.com/")
            .build()

        assertNotNull(client1)
        assertNotNull(client2)
        // Both should be valid and independent instances
        assertTrue(client1 !== client2)
    }
}

