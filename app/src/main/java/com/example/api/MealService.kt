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

class MealService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun analyzeMeal(description: String): MealAnalysisResult? {
        val resultJsonString = analyzeMealWithGemini(description)
        return try {
            val json = JSONObject(resultJsonString)
            MealAnalysisResult(
                calories = json.optInt("calories", 0),
                protein = json.optInt("protein", 0),
                carbs = json.optInt("carbs", 0),
                fat = json.optInt("fat", 0),
                name = json.optString("name", description)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
