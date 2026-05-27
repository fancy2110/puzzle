package com.puzzle.game.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Tongyi Wanxiang (通义万相) AI image generation provider.
 *
 * Requires a DashScope API key from: https://dashscope.console.aliyun.com/
 *
 * Usage:
 *   val provider = TongyiImageProvider(apiKey = "sk-...", httpClient = HttpClient())
 *   val result = provider.generateImage("可爱的小猫咪在草地上玩耍")
 *   // result.imageUrl contains the generated image URL
 */
class TongyiImageProvider(
    private val apiKey: String,
    private val httpClient: HttpClient
) : AIImageProvider {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class TaskRequest(
        val model: String = "wanx2.0-t2i-turbo",
        val input: Input
    ) {
        @Serializable
        data class Input(
            val prompt: String,
            val n: Int = 1,
            val size: String = "1024*1024"
        )
    }

    @Serializable
    data class TaskResponse(
        val output: Output? = null,
        val code: String? = null,
        val message: String? = null
    ) {
        @Serializable
        data class Output(
            val task_id: String,
            val task_status: String
        )
    }

    @Serializable
    data class TaskResult(
        val output: Output? = null,
        val code: String? = null,
        val message: String? = null
    ) {
        @Serializable
        data class Output(
            val task_id: String,
            val task_status: String,
            val results: List<ResultItem>? = null
        ) {
            @Serializable
            data class ResultItem(
                val url: String? = null,
                val code: String? = null,
                val message: String? = null
            )
        }
    }

    override suspend fun generateImage(prompt: String): GeneratedImage {
        if (apiKey.isBlank()) {
            return GeneratedImage(
                id = "mock_${kotlin.random.Random.nextInt(10000, 99999)}",
                prompt = prompt,
                imageUrl = null
            )
        }

        // 1. Submit task
        val request = TaskRequest(
            input = TaskRequest.Input(prompt = prompt)
        )

        val taskResponse: TaskResponse = httpClient.post(
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis"
        ) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            header("X-DashScope-Async", "enable")
            setBody(json.encodeToString(TaskRequest.serializer(), request))
        }.body()

        val taskId = taskResponse.output?.task_id
            ?: throw Exception("Failed to submit task: ${taskResponse.message}")

        // 2. Poll for result
        repeat(30) { attempt ->
            delay(2000)

            val result: TaskResult = httpClient.get(
                "https://dashscope.aliyuncs.com/api/v1/tasks/$taskId"
            ) {
                header("Authorization", "Bearer $apiKey")
            }.body()

            val output = result.output ?: return@repeat
            when (output.task_status) {
                "SUCCEEDED" -> {
                    val imageUrl = output.results?.firstOrNull()?.url
                        ?: throw Exception("No image URL in result")

                    return GeneratedImage(
                        id = taskId,
                        prompt = prompt,
                        imageUrl = imageUrl
                    )
                }
                "FAILED" -> {
                    val errMsg = output.results?.firstOrNull()?.message
                        ?: result.message ?: "Unknown error"
                    throw Exception("Image generation failed: $errMsg")
                }
                "PENDING", "RUNNING" -> {
                    // continue polling
                }
            }
        }

        throw Exception("Image generation timed out after 60s")
    }
}
