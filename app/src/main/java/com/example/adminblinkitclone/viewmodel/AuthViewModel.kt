package com.example.adminblinkitclone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.adminblinkitclone.Utils
import kotlinx.coroutines.flow.MutableStateFlow

class AuthViewModel : ViewModel(){

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser = _isCurrentUser

    init {
        Utils.getAuthInstance().currentUser?.let {
            _isCurrentUser.value = true
        }
    }
}