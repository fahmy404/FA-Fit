package com.example.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// Special sentinel values returned on specific errors
const val RESULT_RATE_LIMITED = "{\"error\":\"rate_limited\"}"
const val RESULT_NETWORK_ERROR = "{\"error\":\"network_error\"}"

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String? = null)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@Serializable
data class GenerateContentResponse(val candidates: List<Candidate>)

@Serializable
data class Candidate(val content: Content)

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }
}

suspend fun analyzeMealWithGemini(prompt: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val maxRetries = 3

    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
        systemInstruction = Content(
            parts = listOf(
                Part(
                    text = "أنت مساعد ذكي لحساب السعرات الحرارية في الأكل المصري والعالمي. " +
                            "سيقوم المستخدم بوصف وجبته. استخرج السعرات الحرارية التقريبية " +
                            "وكمية البروتين والكربوهيدرات والدهون بالجرام. " +
                            "رد بصيغة JSON فقط تحتوي على: calories, protein, carbs, fat, name."
                )
            )
        ),
        generationConfig = GenerationConfig(responseMimeType = "application/json")
    )

    for (attempt in 0 until maxRetries) {
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                return@withContext text
            }
        } catch (e: HttpException) {
            if (e.code() == 429) {
                // On 429, wait 30 seconds then retry (max 2 retries for rate limit)
                if (attempt < 2) {
                    delay(30_000L) // wait 30 seconds before retry
                } else {
                    return@withContext RESULT_RATE_LIMITED
                }
            } else {
                if (attempt < maxRetries - 1) {
                    delay(2000L * (attempt + 1))
                } else {
                    e.printStackTrace()
                    return@withContext RESULT_NETWORK_ERROR
                }
            }
        } catch (e: Exception) {
            val msg = (e.message ?: "") + (e.cause?.message ?: "")
            if (msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true)) {
                if (attempt < 2) {
                    delay(30_000L)
                } else {
                    return@withContext RESULT_RATE_LIMITED
                }
            } else {
                if (attempt < maxRetries - 1) {
                    delay(2000L * (attempt + 1))
                } else {
                    e.printStackTrace()
                    return@withContext RESULT_NETWORK_ERROR
                }
            }
        }
    }

    return@withContext RESULT_NETWORK_ERROR
}