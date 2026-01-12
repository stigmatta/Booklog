package site.odintsov.booklog.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import site.odintsov.booklog.data.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.login(email, pass)
            _isLoading.value = false
            if (result.isSuccess) {
                _user.value = result.getOrNull()
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun signup(email: String, pass: String, name: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.signup(email, pass, name)
            _isLoading.value = false
            if (result.isSuccess) {
                _user.value = result.getOrNull()
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Sign up failed"
            }
        }
    }
    
    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.uploadProfilePicture(uri)
            if (result.isSuccess) {
                _user.value = repository.currentUser // Refresh user state
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Image upload failed"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _user.value = null
    }
}
