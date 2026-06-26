package com.puzzle.game.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * SiliconFlow image generation provider.
 *
 * This is useful as a China-accessible aggregation channel when the product
 * wants to switch among Qwen-Image/Kolors-style models without changing app
 * code. The API key should be provided by a backend or secure runtime config.
 */
class SiliconFlowImageProvider(
    private val apiKey: String,
    private val httpClient: HttpClient,
    private val model: String = "Kwai-Kolors/Kolors",
    private val endpoint: String = "https://api.siliconflow.cn/v1/images/generations"
) : AIImageProvider {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateImage(request: ImageGenerationRequest): GeneratedImage {
        if (apiKey.isBlank()) {
            return GeneratedImage(
                id = "mock_${kotlin.random.Random.nextInt(10000, 99999)}",
                prompt = request.prompt,
                imageUrl = null
            )
        }

        val response = httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(buildRequestBody(request))
        }.bodyAsText()

        val root = json.parseToJsonElement(response).jsonObject
        val imageUrl = root["images"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("url")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: throw Exception(root["message"]?.jsonPrimitive?.contentOrNull ?: "SiliconFlow image URL missing")

        return GeneratedImage(
            id = "siliconflow_${kotlin.random.Random.nextInt(10000, 99999)}",
            prompt = request.prompt,
            imageUrl = imageUrl
        )
    }

    private fun buildRequestBody(request: ImageGenerationRequest): String {
        return buildJsonObject {
            put("model", model)
            put("prompt", request.prompt)
            put("negative_prompt", request.negativePrompt)
            put("image_size", request.size)
            put("batch_size", request.batchSize.coerceIn(1, 4))
            request.seed?.let { put("seed", it) }
        }.toString()
    }
}
