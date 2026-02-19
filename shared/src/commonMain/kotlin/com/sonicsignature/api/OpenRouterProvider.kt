package com.sonicsignature.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1"

/**
 * OpenRouter LLM provider implementation. Uses OpenAI-compatible chat completions API via
 * OpenRouter's unified gateway. Constitution §2.2: No proxying — direct device-to-provider call.
 * Constitution §2.3: Provider-neutral; model ID is user-configured.
 */
class OpenRouterProvider(
        private val httpClient: HttpClient,
        private val apiKey: String,
        private val modelId: String
) : LLMProvider {

        @Serializable
        private data class ChatRequest(val model: String, val messages: List<Message>) {
                @Serializable data class Message(val role: String, val content: String)
        }

        @Serializable
        private data class ChatResponse(val choices: List<Choice>) {
                @Serializable data class Choice(val message: Message)
                @Serializable data class Message(val content: String)
        }

        override suspend fun complete(prompt: String): String {
                val response =
                        httpClient.post("$OPENROUTER_BASE_URL/chat/completions") {
                                header(HttpHeaders.Authorization, "Bearer $apiKey")
                                header("HTTP-Referer", "https://github.com/sonic-signature")
                                header("X-Title", "Sonic Signature")
                                contentType(ContentType.Application.Json)
                                setBody(
                                        ChatRequest(
                                                model = modelId,
                                                messages =
                                                        listOf(
                                                                ChatRequest.Message(
                                                                        role = "user",
                                                                        content = prompt
                                                                )
                                                        )
                                        )
                                )
                        }

                if (!response.status.isSuccess()) {
                        val statusCode = response.status.value
                        val errorBody =
                                try {
                                        response.bodyAsText()
                                } catch (_: Exception) {
                                        ""
                                }
                        val detail = extractError(errorBody)

                        throw when {
                                statusCode == 401 || statusCode == 403 ->
                                        IllegalStateException("OpenRouter auth error: $detail")
                                statusCode == 429 ->
                                        IllegalStateException("OpenRouter rate limit. $detail")
                                statusCode >= 500 ->
                                        IllegalStateException(
                                                "OpenRouter service unavailable ($statusCode). Try again."
                                        )
                                else ->
                                        IllegalStateException(
                                                "OpenRouter error ($statusCode): $detail"
                                        )
                        }
                }

                val chatResponse = response.body<ChatResponse>()
                return chatResponse.choices.firstOrNull()?.message?.content
                        ?: throw IllegalStateException(
                                "Couldn't parse recommendations. Try again or switch models."
                        )
        }

        /** Extract a human-readable message from OpenRouter's JSON error response. */
        private fun extractError(body: String): String {
                // OpenRouter errors: {"error":{"message":"...","code":401}}
                val messageRegex = """"message"\s*:\s*"([^"]+)"""".toRegex()
                return messageRegex.find(body)?.groupValues?.get(1) ?: body.take(200)
        }
}
