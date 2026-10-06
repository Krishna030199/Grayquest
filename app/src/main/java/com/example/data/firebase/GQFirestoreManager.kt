package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.model.Lead
import com.example.data.model.LeadActivity
import com.example.data.model.LeadSource
import com.example.data.model.LeadStage
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GQFirestoreManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val auth: FirebaseAuth = Firebase.auth

    // Mandatory: Explicitly pass named database ID from firebase_applet_config.xml
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _syncStatus = MutableStateFlow("Initialized")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _firebaseUser.value = firebaseAuth.currentUser
            _syncStatus.value = if (firebaseAuth.currentUser != null) {
                "Cloud Connected (${firebaseAuth.currentUser?.email})"
            } else {
                "Offline / Local Only"
            }
        }
    }

    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        return try {
            val webClientId = context.getString(R.string.default_web_client_id)
            val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(request = request, context = activity)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: error("No user returned from Firebase")
                _firebaseUser.value = user
                _syncStatus.value = "Connected as ${user.email}"
                Result.success(user)
            } else {
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("GQFirestoreManager", "Google Sign-In was cancelled by user: ${e.message}")
            Result.failure(Exception("Sign-in cancelled"))
        } catch (e: Exception) {
            Log.e("GQFirestoreManager", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _firebaseUser.value = null
        _syncStatus.value = "Signed Out"
    }

    suspend fun uploadLead(lead: Lead): Result<Unit> {
        if (auth.currentUser == null) {
            return Result.failure(Exception("Must be signed in to sync with Firebase Cloud"))
        }
        return try {
            val leadData = hashMapOf(
                "studentName" to lead.studentName,
                "parentName" to lead.parentName,
                "phone" to lead.phone,
                "city" to lead.city,
                "institutionId" to lead.institutionId,
                "institutionName" to lead.institutionName,
                "courseClass" to lead.courseClass,
                "loanAmount" to lead.loanAmount,
                "source" to lead.source.name,
                "assignedToId" to lead.assignedToId,
                "assignedToName" to lead.assignedToName,
                "stage" to lead.stage.name,
                "lostReason" to (lead.lostReason ?: ""),
                "nextFollowUpDate" to lead.nextFollowUpDate,
                "notes" to lead.notes,
                "createdAt" to lead.createdAt,
                "updatedAt" to lead.updatedAt
            )

            firestore.collection("leads")
                .document(lead.id.toString())
                .set(leadData, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GQFirestoreManager", "Failed to upload lead to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun uploadActivity(activity: LeadActivity): Result<Unit> {
        if (auth.currentUser == null) return Result.failure(Exception("Unauthenticated"))
        return try {
            val actData = hashMapOf(
                "leadId" to activity.leadId.toString(),
                "authorName" to activity.authorName,
                "authorRole" to activity.authorRole,
                "actionType" to activity.actionType,
                "description" to activity.description,
                "timestamp" to activity.timestamp
            )

            firestore.collection("leads")
                .document(activity.leadId.toString())
                .collection("activities")
                .document(activity.id.toString())
                .set(actData, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
