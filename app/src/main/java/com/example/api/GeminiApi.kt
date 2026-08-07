package com.example.api

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun analyzeMealWithGemini(prompt: String): String = withContext(Dispatchers.IO) {
    return@withContext try {
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-2.0-flash",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                },
                systemInstruction = com.google.firebase.ai.type.content {
                    text("أنت مساعد ذكي لحساب السعرات الحرارية في الأكل المصري والعالمي. سيقوم المستخدم بوصف وجبته. استخرج السعرات الحرارية التقريبية وكمية البروتين والكربوهيدرات والدهون بالجرام. رد بصيغة JSON فقط تحتوي على: calories, protein, carbs, fat, name.")
                }
            )

        val response = model.generateContent(prompt)
        response.text ?: "{}"
    } catch (e: Exception) {
        e.printStackTrace()
        "{}"
    }
}