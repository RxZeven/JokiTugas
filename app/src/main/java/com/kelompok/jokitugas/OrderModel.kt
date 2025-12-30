package com.kelompok.jokitugas

// Defines the structure of an Order
data class OrderModel(
    val id: String,
    val serviceName: String,
    val deadline: String,
    val price: String,
    val status: String
)

// A temporary storage object to hold data while the app is running
object DummyData {
    val orderHistory = mutableListOf<OrderModel>()
}
