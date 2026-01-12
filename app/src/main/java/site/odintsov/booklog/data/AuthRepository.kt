package site.odintsov.booklog.data

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import site.odintsov.booklog.data.aws.S3Service

class AuthRepository(private val context: Context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val s3Service = S3Service()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun login(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(email: String, pass: String, name: String): Result<FirebaseUser?> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            
            if (user != null && name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                user.reload().await()
            }
            
            Result.success(firebaseAuth.currentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(uri: Uri): Result<Uri> {
        return withContext(Dispatchers.IO) {
            try {
                val user = currentUser ?: return@withContext Result.failure(Exception("No user logged in"))
                
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("Could not open file"))
                
                // Upload to AWS S3
                val imageUrl = s3Service.uploadFile(inputStream, "image/jpeg")
                val dataUri = Uri.parse(imageUrl)
                
                // Save the public S3 URL to Firebase User Profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(dataUri)
                    .build()
                
                user.updateProfile(profileUpdates).await()
                user.reload().await()
                
                Result.success(dataUri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
