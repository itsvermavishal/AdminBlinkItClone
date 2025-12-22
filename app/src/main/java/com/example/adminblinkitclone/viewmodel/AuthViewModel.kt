package com.example.adminblinkitclone.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.adminblinkitclone.model.Admin
import com.example.adminblinkitclone.Utils
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel(){
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId

    private val _otpSent = MutableStateFlow<Boolean?>(null)
    val otpSent: StateFlow<Boolean?> = _otpSent

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError

    private val _isSignedInSuccessfully = MutableStateFlow(false)
    val isSignedInSuccessfully = _isSignedInSuccessfully

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser = _isCurrentUser

    init {
        Utils.getAuthInstance().currentUser?.let {
            _isCurrentUser.value = true
        }
    }
    fun sendOTP(userNumber: String, activity: Activity) {
        _otpSent.value = null // Reset before sending
        _otpError.value = null

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification, may not need UI update
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("AuthViewModel", "Verification failed: ${e.message}", e)
                _otpSent.value = false
                _otpError.value = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid phone number."
                    is FirebaseTooManyRequestsException -> "Too many requests, please try again later."
                    is FirebaseAuthMissingActivityForRecaptchaException -> "Recaptcha verification failed."
                    else -> e.localizedMessage ?: "OTP sending failed. Try again."
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _verificationId.value = verificationId
                _otpSent.value = true
            }
        }
        val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
            .setPhoneNumber("+91$userNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: Admin) {
        val credential = PhoneAuthProvider.getCredential(verificationId.value.toString(), otp)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token =  task.result
            user.adminToken = token


            Utils.getAuthInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = Utils.getCurrentUserId()
                        val updatedUser = user.copy(uid = uid)

                        // Save user data to Realtime Database
                        FirebaseDatabase.getInstance()
                            .getReference("Admin")
                            .child("AdminInfo")
                            .child(uid!!)
                            .setValue(updatedUser)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    _isSignedInSuccessfully.value = true
                                } else {
                                    Log.e("AuthViewModel", "User save failed: ${it.exception?.message}")
                                }
                            }
                    } else {
                        _otpError.value = task.exception?.localizedMessage ?: "Sign-in failed."
                        Log.e("AuthViewModel", "Sign-in failed: ${task.exception?.message}")
                    }
                }
        }
    }

    fun createOrUpdateUser(user: Admin) {
        val uid = user.uid ?: return
        FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(uid)
            .setValue(user)
    }
}