package com.example.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

data class MealAnalysisResult(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val name: String
)

// Error types returned by analyzeMeal
sealed class MealAnalysisError {
    object RateLimited : MealAnalysisError()
    object NetworkError : MealAnalysisError()
    object ParseError : MealAnalysisError()
}

data class MealAnalysisResponse(
    val result: MealAnalysisResult? = null,
    val error: MealAnalysisError? = null
) {
    val isSuccess: Boolean get() = result != null && error == null
}

class MealService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun analyzeMeal(description: String): MealAnalysisResult? {
        val response = analyzeMealFull(description)
        return response.result
    }

    suspend fun analyzeMealFull(description: String): MealAnalysisResponse {
        val resultJsonString = analyzeMealWithGemini(description)

        // Check for specific error sentinels
        return when (resultJsonString) {
            RESULT_RATE_LIMITED -> MealAnalysisResponse(error = MealAnalysisError.RateLimited)
            RESULT_NETWORK_ERROR -> MealAnalysisResponse(error = MealAnalysisError.NetworkError)
            else -> {
                try {
                    val json = JSONObject(resultJsonString)

                    // Check if the JSON itself contains an error field
                    if (json.has("error")) {
                        return MealAnalysisResponse(error = MealAnalysisError.NetworkError)
                    }

                    val result = MealAnalysisResult(
                        calories = json.optInt("calories", 0),
                        protein = json.optInt("protein", 0),
                        carbs = json.optInt("carbs", 0),
                        fat = json.optInt("fat", 0),
                        name = json.optString("name", description)
                    )

                    if (result.calories == 0) {
                        MealAnalysisResponse(error = MealAnalysisError.ParseError)
                    } else {
                        MealAnalysisResponse(result = result)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    MealAnalysisResponse(error = MealAnalysisError.ParseError)
                }
            }
        }
    }

    suspend fun saveMealToFirestore(result: MealAnalysisResult): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            val mealData = hashMapOf(
                "name" to result.name,
                "calories" to result.calories,
                "protein" to result.protein,
                "carbs" to result.carbs,
                "fat" to result.fat,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(userId)
                .collection("meals").add(mealData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
