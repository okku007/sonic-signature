package com.sonicsignature.engine

import com.sonicsignature.util.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecommendationParserTest {

    @Test
    fun `parse valid JSON returns recommendations`() {
        val validJson =
                """
            [
              {
                "name": "Aria Snow",
                "brand": "Moondrop",
                "priceINR": 6500,
                "driverType": "DD",
                "soundSignature": "NEUTRAL",
                "crinacleGrade": "B+",
                "justification": "Great for vocal lovers.",
                "compatibilityScore": 85,
                "strengths": ["Vocals", "Build"],
                "tradeOffs": ["Sub-bass"],
                "tonalCategory": "Neutral"
              }
            ]
        """.trimIndent()

        val result = RecommendationParser.parse(validJson)

        assertTrue(result is Result.Success)
        assertEquals(1, result.data.size)
        assertEquals("Aria Snow", result.data[0].name)
        assertEquals(85, result.data[0].compatibilityScore)
        assertEquals(2, result.data[0].strengths.size)
    }

    @Test
    fun `parse JSON wrapped in markdown fences returns recommendations`() {
        val markdownJson =
                """
            Here are your recommendations:
            ```json
            [
              {
                "name": "Chu II",
                "brand": "Moondrop",
                "priceINR": 1800,
                "driverType": "DD",
                "soundSignature": "V_SHAPED",
                "crinacleGrade": "C+",
                "justification": "Incredible value.",
                "compatibilityScore": 90,
                "strengths": ["Bass", "Price"],
                "tradeOffs": ["Treble spice"],
                "tonalCategory": "V-Shape"
              }
            ]
            ```
            Hope this helps!
        """.trimIndent()

        val result = RecommendationParser.parse(markdownJson)

        assertTrue(result is Result.Success)
        assertEquals(1, result.data.size)
        assertEquals("Chu II", result.data[0].name)
        assertEquals(90, result.data[0].compatibilityScore)
    }

    @Test
    fun `parse invalid JSON returns Error`() {
        val invalidJson = "This is not JSON"
        val result = RecommendationParser.parse(invalidJson)
        assertTrue(result is Result.Error)
    }

    @Test
    fun `parse empty JSON array returns Error`() {
        val emptyJson = "[]"
        val result = RecommendationParser.parse(emptyJson)
        assertTrue(result is Result.Error)
    }
}
