package com.sonicsignature.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

/**
 * Gemini LLM provider implementation. Calls the Gemini REST API directly from the user's device
 * using their own API key. Constitution §2.2: No proxying — direct device-to-provider call.
 *
 * Uses gemini-2.0-flash by default. Includes automatic retry with exponential backoff for 429
 * rate-limit responses.
 */
class GeminiProvider(
        private val httpClient: HttpClient,
        private val apiKey: String,
        private val model: String = "gemini-2.0-flash-lite"
) : LLMProvider {

        @Serializable
        private data class GeminiRequest(val contents: List<Content>) {
                @Serializable data class Content(val parts: List<Part>)
                @Serializable data class Part(val text: String)
        }

        @Serializable
        private data class GeminiResponse(val candidates: List<Candidate>) {
                @Serializable data class Candidate(val content: Content)
                @Serializable data class Content(val parts: List<Part>)
                @Serializable data class Part(val text: String)
        }

        companion object {
                private const val MAX_RETRIES = 3
                private const val INITIAL_DELAY_MS = 5000L
        }

        override suspend fun complete(prompt: String): String {
                var lastException: Exception? = null

                repeat(MAX_RETRIES + 1) { attempt ->
                        val response =
                                httpClient.post("$GEMINI_BASE_URL/$model:generateContent") {
                                        parameter("key", apiKey)
                                        contentType(ContentType.Application.Json)
                                        setBody(
                                                GeminiRequest(
                                                        contents =
                                                                listOf(
                                                                        GeminiRequest.Content(
                                                                                parts =
                                                                                        listOf(
                                                                                                GeminiRequest
                                                                                                        .Part(
                                                                                                                text =
                                                                                                                        prompt
                                                                                                        )
                                                                                        )
                                                                        )
                                                                )
                                                )
                                        )
                                }

                        if (response.status.isSuccess()) {
                                val geminiResponse = response.body<GeminiResponse>()
                                return geminiResponse
                                        .candidates
                                        .firstOrNull()
                                        ?.content
                                        ?.parts
                                        ?.firstOrNull()
                                        ?.text
                                        ?: throw IllegalStateException(
                                                "Couldn't parse recommendations. Try again or switch models."
                                        )
                        }

                        val statusCode = response.status.value
                        // Read the actual error body from Google for diagnostics
                        val errorBody =
                                try {
                                        response.bodyAsText()
                                } catch (_: Exception) {
                                        ""
                                }

                        // If quota limit is literally 0, don't bother retrying — it will never
                        // succeed
                        if (statusCode == 429 && errorBody.contains("limit: 0")) {
                                throw IllegalStateException(
                                        "Your Gemini free-tier quota is 0 for $model in your region. " +
                                                "Options: 1) Enable billing at console.cloud.google.com, " +
                                                "2) Use OpenRouter provider instead."
                                )
                        }

                        // Retry on 429 with exponential backoff
                        if (statusCode == 429 && attempt < MAX_RETRIES) {
                                val delayMs = INITIAL_DELAY_MS * (1 shl attempt) // 2s, 4s
                                delay(delayMs)
                                lastException =
                                        IllegalStateException(
                                                "Rate limited (attempt ${attempt + 1})"
                                        )
                                return@repeat
                        }

                        throw when {
                                statusCode == 429 ->
                                        IllegalStateException(
                                                "Gemini rate limit hit after ${MAX_RETRIES + 1} attempts. " +
                                                        "Detail: ${extractGoogleError(errorBody)}"
                                        )
                                statusCode == 401 || statusCode == 403 ->
                                        IllegalStateException(
                                                "API key invalid or Gemini API not enabled. " +
                                                        "Detail: ${extractGoogleError(errorBody)}"
                                        )
                                statusCode >= 500 ->
                                        IllegalStateException(
                                                "Gemini service unavailable ($statusCode). Try again."
                                        )
                                else ->
                                        IllegalStateException(
                                                "Gemini API error ($statusCode): ${extractGoogleError(errorBody)}"
                                        )
                        }
                }

                throw lastException
                        ?: IllegalStateException("Gemini API request failed after retries.")
        }

        /** Extract a human-readable message from Google's JSON error response. */
        private fun extractGoogleError(body: String): String {
                // Google errors look like:
                // {"error":{"code":429,"message":"...","status":"RESOURCE_EXHAUSTED"}}
                val messageRegex = """"message"\s*:\s*"([^"]+)"""".toRegex()
                return messageRegex.find(body)?.groupValues?.get(1) ?: body.take(200)
        }
}
