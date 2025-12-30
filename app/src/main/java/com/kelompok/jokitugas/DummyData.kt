package com.kelompok.jokitugas

// Change the name here
data class DummyOrderModel(
    val id: String,
    val serviceName: String,
    val deadline: String,
    val price: String,
    val status: String = "Menunggu Pembayaran"
)

object DummyDataRepository {
    // Update the reference here as well
    val orderHistory = mutableListOf<DummyOrderModel>(
        DummyOrderModel(
            id = System.currentTimeMillis().toString(),
            serviceName = "Coding Android",
            deadline = "20 Juni 2025",
            price = "Rp 250.000",
            status = "Menunggu Pembayaran"
        )
    )

    fun addOrder(order: DummyOrderModel) {
        orderHistory.add(order)
    }
}
