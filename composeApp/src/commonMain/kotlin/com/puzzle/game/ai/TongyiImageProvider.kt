package com.puzzle.game.ai

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * Tongyi Wanxiang (通义万相 / 阿里云百炼) image generation provider.
 *
 * Requires a DashScope/Bailian API key.
 * Uses the V2 async protocol so it can work with wan2.6 and older V2 models.
 *
 * Usage:
 *   val provider = TongyiImageProvider(apiKey = "sk-...", httpClient = HttpClient())
 *   val result = provider.generateImage(ImageGenerationRequest(prompt = "..."))
 */
class TongyiImageProvider(
    private val apiKey: String,
    private val httpClient: HttpClient,
    private val model: String = "wan2.6-t2i",
    private val endpoint: String = "https://dashscope.aliyuncs.com"
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

        val taskJson = httpClient.post(
            "$endpoint/api/v1/services/aigc/image-generation/generation"
        ) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            header("X-DashScope-Async", "enable")
            setBody(buildRequestBody(request))
        }.bodyAsText()

        val taskId = json.parseToJsonElement(taskJson)
            .jsonObject["output"]
            ?.jsonObject
            ?.get("task_id")
            ?.asString()
            ?: throw Exception("Failed to submit task: ${extractErrorMessage(taskJson)}")

        repeat(30) { attempt ->
            delay(2000)

            val resultJson = httpClient.get(
                "$endpoint/api/v1/tasks/$taskId"
            ) {
                header("Authorization", "Bearer $apiKey")
            }.bodyAsText()

            val resultRoot = json.parseToJsonElement(resultJson).jsonObject
            val output = resultRoot["output"]?.jsonObject ?: return@repeat
            when (output["task_status"]?.asString()) {
                "SUCCEEDED" -> {
                    val imageUrl = findFirstImageUrl(output)
                        ?: throw Exception("No image URL in result")

                    return GeneratedImage(
                        id = taskId,
                        prompt = request.prompt,
                        imageUrl = imageUrl
                    )
                }
                "FAILED" -> {
                    val errMsg = output["message"]?.asString()
                        ?: resultRoot["message"]?.asString()
                        ?: "Unknown error"
                    throw Exception("Image generation failed: $errMsg")
                }
                "PENDING", "RUNNING" -> {
                    // continue polling
                }
            }
        }

        throw Exception("Image generation timed out after 60s")
    }

    private fun buildRequestBody(request: ImageGenerationRequest): String {
        val dashScopeSize = request.size.replace("x", "*")
        val root = buildJsonObject {
            put("model", model)
            put("input", buildJsonObject {
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put("text", request.prompt)
                            })
                        })
                    })
                })
            })
            put("parameters", buildJsonObject {
                put("prompt_extend", true)
                put("watermark", false)
                put("n", request.batchSize.coerceIn(1, 4))
                put("negative_prompt", request.negativePrompt)
                put("size", dashScopeSize)
                request.seed?.let { put("seed", it) }
            })
        }
        return root.toString()
    }

    private fun JsonElement.asString(): String? = (this as? JsonPrimitive)?.contentOrNull

    private fun findFirstImageUrl(element: JsonElement): String? {
        return when (element) {
            is JsonObject -> {
                element["image"]?.asString()
                    ?: element["url"]?.asString()
                    ?: element.values.firstNotNullOfOrNull { findFirstImageUrl(it) }
            }
            is JsonArray -> element.firstNotNullOfOrNull { findFirstImageUrl(it) }
            else -> null
        }
    }

    private fun extractErrorMessage(response: String): String {
        return runCatching {
            val root = json.parseToJsonElement(response).jsonObject
            root["message"]?.asString() ?: root["code"]?.asString() ?: response
        }.getOrElse { response }
    }
}
