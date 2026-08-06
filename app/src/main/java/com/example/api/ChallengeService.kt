package com.example.api

import com.example.ui.viewmodels.Challenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChallengeService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getActiveChallenges(): List<Challenge> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            // Get challenges this user created or joined
            val snapshot = db.collection("challenges")
                .whereArrayContains("members", userId)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                val title = doc.getString("title") ?: ""
                val timeLeft = doc.getString("timeLeft") ?: ""
                val rank = doc.getLong("rank")?.toInt() ?: 1
                val code = doc.getString("code") ?: ""
                val type = doc.getString("type") ?: "calories"
                val targetValue = doc.getLong("targetValue")?.toInt() ?: 0
                Challenge(id, title, timeLeft, rank, code, type, targetValue)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createChallenge(challenge: Challenge): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            val challengeData = hashMapOf(
                "id" to challenge.id,
                "title" to challenge.title,
                "timeLeft" to challenge.timeLeft,
                "rank" to challenge.rank,
                "code" to challenge.code,
                "type" to challenge.type,
                "targetValue" to challenge.targetValue,
                "createdBy" to userId,
                "members" to listOf(userId),
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("challenges").document(challenge.code).set(challengeData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun joinChallenge(code: String): Challenge? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val docRef = db.collection("challenges").document(code)
            val doc = docRef.get().await()
            if (doc.exists()) {
                // Add user to members list
                @Suppress("UNCHECKED_CAST")
                val members = (doc.get("members") as? List<String> ?: emptyList()).toMutableList()
                if (!members.contains(userId)) {
                    members.add(userId)
                    docRef.update("members", members).await()
                }
                val id = doc.getLong("id")?.toInt() ?: 0
                val title = doc.getString("title") ?: ""
                val timeLeft = doc.getString("timeLeft") ?: ""
                val rank = doc.getLong("rank")?.toInt() ?: 1
                val type = doc.getString("type") ?: "calories"
                val targetValue = doc.getLong("targetValue")?.toInt() ?: 0
                Challenge(id, title, timeLeft, rank, code, type, targetValue)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
