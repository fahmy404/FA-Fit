package com.example.utils

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleAuthHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    
    suspend fun getGoogleIdToken(): Result<String> {
        return try {
            val webClientId = getWebClientId()
            if (webClientId.isEmpty()) {
                return Result.failure(Exception("لم يتم إعداد تسجيل الدخول بحساب جوجل. يرجى إضافة SHA-1 وتحديث google-services.json"))
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Result.failure(Exception("بيانات الاعتماد غير صحيحة"))
            }
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthHelper", "GetCredentialException: ${e.message}", e)
            Result.failure(Exception("تم إلغاء تسجيل الدخول أو حدث خطأ: ${e.message}"))
        } catch (e: Exception) {
            Log.e("GoogleAuthHelper", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun getWebClientId(): String {
        return try {
            context.getString(context.resources.getIdentifier("default_web_client_id", "string", context.packageName))
        } catch (e: Exception) {
            ""
        }
    }
}
