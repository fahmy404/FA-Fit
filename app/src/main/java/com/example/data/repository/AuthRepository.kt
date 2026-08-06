package com.example.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener {
            trySend(it.currentUser)
        }
        auth?.addAuthStateListener(listener)
        awaitClose { auth?.removeAuthStateListener(listener) }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        if (auth == null) return Result.failure(Exception("Firebase is not initialized. Please add google-services.json."))
        return try {
            val result = auth!!.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        if (auth == null) return Result.failure(Exception("Firebase is not initialized. Please add google-services.json."))
        return try {
            val result = auth!!.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> {
        if (auth == null) return Result.failure(Exception("Firebase is not initialized. Please add google-services.json."))
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth!!.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth?.signOut()
    }
}
