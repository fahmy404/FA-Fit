package com.example.data.repository

import com.example.data.model.User
import com.example.data.model.Challenge
import com.example.data.model.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    // --- Users ---
    suspend fun createUser(user: User): Result<Unit> {
        if (db == null) return Result.failure(Exception("Firestore not initialized"))
        return try {
            db!!.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User?> {
        if (db == null) return Result.failure(Exception("Firestore not initialized"))
        return try {
            val snapshot = db!!.collection("users").document(userId).get().await()
            Result.success(snapshot.toObject(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Challenges ---
    suspend fun createChallenge(challenge: Challenge): Result<Unit> {
        if (db == null) return Result.failure(Exception("Firestore not initialized"))
        return try {
            val docRef = if (challenge.id.isEmpty()) {
                db!!.collection("challenges").document()
            } else {
                db!!.collection("challenges").document(challenge.id)
            }
            val newChallenge = challenge.copy(id = docRef.id)
            docRef.set(newChallenge).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChallenges(): Result<List<Challenge>> {
        if (db == null) return Result.failure(Exception("Firestore not initialized"))
        return try {
            val snapshot = db!!.collection("challenges").get().await()
            val challenges = snapshot.toObjects(Challenge::class.java)
            Result.success(challenges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Exercises ---
    suspend fun addExercise(exercise: Exercise): Result<Unit> {
        if (db == null) return Result.failure(Exception("Firestore not initialized"))
        return try {
            val docRef = if (exercise.id.isEmpty()) {
                db!!.collection("exercises").document()
            } else {
                db!!.collection("exercises").document(exercise.id)
            }
            val newExercise = exercise.copy(id = docRef.id)
            docRef.set(newExercise).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserExercises(userId: String): Result<List<Exercise>> {
        if (db == null) return Result.failure(Exception("Firestore not initialized"))
        return try {
            val snapshot = db!!.collection("exercises")
                .whereEqualTo("userId", userId)
                .get().await()
            val exercises = snapshot.toObjects(Exercise::class.java)
            Result.success(exercises)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
