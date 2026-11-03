package com.example.data.remote

import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
) {

    private val systemInstructionText = """
        You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), an ultra-sophisticated AI assistant.
        You communicate with polite, refined British composure, razor-sharp brevity, and supreme competence.
        Keep answers concise, direct, informative, and formatted cleanly for mobile reading.
        Never generate overly long walls of text unless explicitly requested.
        When acknowledging device actions or status queries, sound like Tony Stark's legendary AI.
    """.trimIndent()

    fun streamChat(
        apiKey: String,
        history: List<ChatMessage>,
        newPrompt: String,
        modelName: String = "gemini-2.5-flash"
    ): Flow<String> = flow {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            emit("API key is not configured. Please supply a valid Gemini API key in the AI Studio Secrets panel or in App Settings.")
            return@flow
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:streamGenerateContent?alt=sse&key=$apiKey"

        val rootJson = JSONObject().apply {
            // System instructions
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstructionText)
                    })
                })
            })

            // Conversation history & current prompt
            val contentsArray = JSONArray()
            // Take up to last 12 messages for context to keep latency fast
            val recentHistory = history.takeLast(12)
            for (msg in recentHistory) {
                if (msg.sender == MessageSender.SYSTEM || msg.text.isBlank()) continue
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", msg.text)
                        })
                    })
                })
            }

            // Append current user message
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", newPrompt)
                    })
                })
            })

            put("contents", contentsArray)

            // Generation config
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        var hasEmittedAnyChunk = false
        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            emit("Network link disrupted. Unable to reach Gemini core: ${e.localizedMessage ?: "Connection error"}")
            return@flow
        }

        response.use { resp ->
            if (!resp.isSuccessful) {
                val errorBody = resp.body?.string() ?: ""
                val errorMsg = try {
                    JSONObject(errorBody).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "HTTP ${resp.code}: ${resp.message}"
                }
                emit("Gemini protocol returned an error: $errorMsg")
                return@flow
            }

            val body = resp.body ?: run {
                emit("Empty response received from AI server.")
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.startsWith("data:")) {
                    val dataPayload = currentLine.removePrefix("data:").trim()
                    if (dataPayload.isEmpty() || dataPayload == "[DONE]") continue

                    try {
                        val chunkObj = JSONObject(dataPayload)
                        val candidates = chunkObj.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text", "")
                                if (text.isNotEmpty()) {
                                    hasEmittedAnyChunk = true
                                    emit(text)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // ignore malformed chunk
                    }
                }
            }

            if (!hasEmittedAnyChunk) {
                emit("Command processed, but no audible response was received.")
            }
        }
    }.flowOn(Dispatchers.IO)
}
