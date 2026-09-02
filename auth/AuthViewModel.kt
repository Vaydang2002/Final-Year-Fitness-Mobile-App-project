package com.example.personalisedfitnessmobileapplication.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedfitnessmobileapplication.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _signUpSuccess = MutableLiveData<Boolean>()
    val signUpSuccess: LiveData<Boolean> = _signUpSuccess

    private val _fingerprintUser = MutableLiveData<Map<String, Any>?>()
    val fingerprintUser: LiveData<Map<String, Any>?> = _fingerprintUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(email, password)
            result.onSuccess {
                _user.value = it
                _errorMessage.value = null
            }.onFailure {
                _errorMessage.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun signUp(
        initial: String,
        firstName: String,
        surname: String,
        email: String,
        phone: String,
        age: Int,
        password: String,
        isFingerprintEnabled: Boolean,
        fingerprintId: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signUp(
                initial, firstName, surname, email, phone, age, password,
                isFingerprintEnabled, fingerprintId
            )
            result.onSuccess {
                _signUpSuccess.value = true
                _errorMessage.value = null
            }.onFailure {
                _errorMessage.value = it.message
                _signUpSuccess.value = false
            }
            _isLoading.value = false
        }
    }

    fun loginWithFingerprint(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getUserByFingerprintId(id)
            result.onSuccess {
                _fingerprintUser.value = it
                _errorMessage.value = null
            }.onFailure {
                _errorMessage.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}