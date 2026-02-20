package com.sonicsignature.engine

import com.sonicsignature.model.IEMRecommendation
import com.sonicsignature.util.Result
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Parses the raw LLM text response into a list of IEMRecommendation objects.
 * Handles markdown code fences that some models wrap around JSON.
 */
object RecommendationParser {

    fun parse(llmResponse: String): Result<List<IEMRecommendation>> {
        val cleaned = llmResponse
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        // Find the JSON array bounds in case the model added any surrounding text
        val startIndex = cleaned.indexOf('[')
        val endIndex = cleaned.lastIndexOf(']')

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            return Result.Error("Couldn't parse recommendations. Try again or switch models.")
        }

        val jsonArray = cleaned.substring(startIndex, endIndex + 1)

        return try {
            val recommendations = json.decodeFromString<List<IEMRecommendation>>(jsonArray)
            if (recommendations.isEmpty()) {
                Result.Error("Couldn't parse recommendations. Try again or switch models.")
            } else {
                Result.Success(recommendations)
            }
        } catch (e: Exception) {
            Result.Error("Couldn't parse recommendations. Try again or switch models.", e)
        }
    }
}
