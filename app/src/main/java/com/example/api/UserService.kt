package com.example.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val name: String = "",
    val gender: String = "",
    val age: Int = 0,
    val height: Int = 0,
    val weight: Int = 0,
    val goal: String = "",
    val activityLevel: String = "",
    val calorieGoal: Int = 0
)

class UserService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun getProfile(): UserProfile? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val doc = kotlinx.coroutines.withTimeoutOrNull(5000) {
                db.collection("users").document(userId).get().await()
            }
            if (doc != null && doc.exists()) {
                UserProfile(
                    name = doc.getString("name") ?: "",
                    gender = doc.getString("gender") ?: "",
                    age = doc.getLong("age")?.toInt() ?: 0,
                    height = doc.getLong("height")?.toInt() ?: 0,
                    weight = doc.getLong("weight")?.toInt() ?: 0,
                    goal = doc.getString("goal") ?: "",
                    activityLevel = doc.getString("activityLevel") ?: "",
                    calorieGoal = doc.getLong("calorieGoal")?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun saveProfile(profile: UserProfile): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(userId).set(profile).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}