package com.kelompok.jokitugas

// DATA BASE SEMENTARA SEBELUM MENGGUNAKAN FIREBASE
data class OrderModel(
    val serviceName: String,
    val deadline: String,
    val price: String,
    val status: String = "Menunggu Pembayaran"
)

// Object Singleton (Database Sementara di RAM)
object DummyData {
    // List untuk menampung semua pesanan
    val orderHistory = mutableListOf<OrderModel>()
}