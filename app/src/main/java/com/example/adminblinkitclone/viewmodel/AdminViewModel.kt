package com.example.adminblinkitclone.viewmodel

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.model.Product
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class AdminViewModel : ViewModel() {

    private val _isImagesUploaded = MutableStateFlow(false)
    val isImagesUploaded: StateFlow<Boolean> = _isImagesUploaded

    private val _downloadedUrls = MutableStateFlow(arrayListOf<String>())
    val downloadedUrls: StateFlow<ArrayList<String>> = _downloadedUrls

    private val _productSaved = MutableStateFlow(false)
    val isProductSaved: StateFlow<Boolean> = _productSaved

    fun saveInDB(imageUris: ArrayList<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = Utils.getCurrentUserId()?.replace("/", "_") ?: "unknown_user"
                val storageRef = FirebaseStorage.getInstance().reference

                val downloadUrls = imageUris.map { uri ->
                    async {
                        try {
                            val imageRef = storageRef.child(userId)
                                .child("productImages")
                                .child(UUID.randomUUID().toString())

                            // Debug log
                            println("Uploading image: $uri")

                            imageRef.putFile(uri).await()
                            val url = imageRef.downloadUrl.await().toString()
                            println("✅ Uploaded -> $url")
                            url
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }
                    }
                }.awaitAll().filter { it.isNotEmpty() }

                if (downloadUrls.isNotEmpty()) {
                    _downloadedUrls.value = ArrayList(downloadUrls)
                    _isImagesUploaded.value = true
                } else {
                    _isImagesUploaded.value = false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _isImagesUploaded.value = false
            }
        }
    }

    fun saveProduct(product: Product){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").setValue(product)
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productRandomId}").setValue(product)
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productRandomId}").setValue(product)
                            .addOnSuccessListener {
                                _productSaved.value = true
                            }
                            .addOnFailureListener {
                                _productSaved.value = false
                            }
                    }
            }

    }
}
