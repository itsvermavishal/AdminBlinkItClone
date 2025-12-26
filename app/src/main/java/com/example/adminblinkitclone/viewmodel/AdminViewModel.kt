package com.example.adminblinkitclone.viewmodel

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminblinkitclone.utils.Utils
import com.example.adminblinkitclone.model.CartProductsTable
import com.example.adminblinkitclone.model.Orders
import com.example.adminblinkitclone.model.Product
import com.example.adminblinkitclone.models.Notification
import com.example.adminblinkitclone.models.NotificationData
import com.example.userblinkitclone.api.ApiUtilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
                FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").setValue(product)
                            .addOnSuccessListener {
                                _productSaved.value = true
                            }
                            .addOnFailureListener {
                                _productSaved.value = false
                            }
                    }
            }

    }

    fun fetchAllProducts(category: String): Flow<ArrayList<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    if (category == "All" || prod?.productCategory == category){
                        products.add(prod!!)
                    }
                }
                trySend(products).isSuccess
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        db.addValueEventListener(eventListener)

        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun savingUpdateProducts(product: Product){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").setValue(product)
    }

    fun getAllProducts(): Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children){
                    val order = orders.getValue(Orders::class.java)
                    if (order != null){
                        orderList.add(order)
                    }
                }
                trySend(orderList).isSuccess
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)

        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun getOrderdProducts(orderId : String): Flow<List<CartProductsTable>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun updateOrderStatus(orderId: String, status : Int){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId).child("orderStatus").setValue(status)
    }

    suspend fun sendNotification(orderId: String, title: String, message: String){
        val getToken = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId).child("orderingUserId").get()
        getToken.addOnSuccessListener { task ->
            val userUID = task.getValue(String::class.java)
            val userToken = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(userUID!!).child("userToken").get()
            userToken.addOnCompleteListener {
                val notification = Notification(it.result.getValue(String::class.java), NotificationData(title, message))
                ApiUtilities.notificationApi.sendNotification(notification)
                    .enqueue(object : Callback<Notification> {
                        override fun onResponse(
                            call: Call<Notification?>,
                            response: Response<Notification?>
                        ) {
                            if (response.isSuccessful){
                                Log.d("GGG", "Notification sent")
                            }
                        }

                        override fun onFailure(
                            call: Call<Notification?>,
                            t: Throwable
                        ) {
                            TODO("Not yet implemented")
                        }
                    })
            }
        }
    }

    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }
}
