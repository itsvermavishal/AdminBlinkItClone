package com.example.adminblinkitclone.model


data class Orders(
    val orderId: String? = null,
    val orderList: List<CartProductsTable>? = null,
    val userAddress: String? = null,
    val orderStatus: Int? = 0,
    val orderDate: String? = null,
    val orderingUserId: String? = null
)
