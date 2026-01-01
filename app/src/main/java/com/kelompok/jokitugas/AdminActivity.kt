package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        listenToOrders()
    }

    override fun onPause() {
        super.onPause()
        // Hentikan listener saat aplikasi tidak aktif agar hemat kuota/baterai
        snapshotListener?.remove()
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun listenToOrders() {
        // Gunakan addSnapshotListener untuk Real-Time Updates
        snapshotListener = db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Listen failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    binding.containerActive.removeAllViews()
                    binding.containerHistory.removeAllViews()

                    var hasData = false

                    for (document in snapshots) {
                        hasData = true
                        val serviceName = document.getString("serviceTitle") ?: "Layanan"
                        val specs = document.getString("specs") ?: ""
                        val status = document.getString("status") ?: "Diproses"

                        val displayName = if (specs.isNotEmpty()) {
                            val shortSpec = specs.split(",")[0].trim()
                            "$serviceName ($shortSpec)"
                        } else {
                            serviceName
                        }

                        val order = OrderModel(
                            id = document.getString("id") ?: document.id,
                            serviceName = displayName,
                            deadline = document.getString("deadline") ?: "-",
                            price = document.getString("price") ?: "-",
                            status = status
                        )

                        // PILAH BERDASARKAN STATUS
                        if (status == "Selesai" || status == "Dibatalkan") {
                            addAdminOrderCard(order, binding.containerHistory, isHistory = true)
                        } else {
                            // Status: Diproses, Dikerjakan
                            addAdminOrderCard(order, binding.containerActive, isHistory = false)
                        }
                    }

                    if (!hasData) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                    }
                }
            }
    }

    private fun addAdminOrderCard(order: OrderModel, container: android.widget.LinearLayout, isHistory: Boolean) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_order_card, container, false)

        val tvName = cardView.findViewById<TextView>(R.id.tvServiceName)
        val tvDate = cardView.findViewById<TextView>(R.id.tvDeadline)
        val tvPrice = cardView.findViewById<TextView>(R.id.tvPrice)
        val tvStatus = cardView.findViewById<TextView>(R.id.tvStatus)
        val btnDetail = cardView.findViewById<TextView>(R.id.btnDetail)

        tvName.text = order.serviceName
        tvDate.text = order.deadline
        tvPrice.text = order.price
        tvStatus.text = order.status

        // Warnai status agar lebih jelas
        when (order.status) {
            "Selesai" -> tvStatus.setBackgroundColor(0xFF4CAF50.toInt()) // Hijau
            "Dibatalkan" -> tvStatus.setBackgroundColor(0xFFF44336.toInt()) // Merah
            "Dikerjakan" -> tvStatus.setBackgroundColor(0xFF2196F3.toInt()) // Biru
            else -> tvStatus.setBackgroundColor(0xFFFF9800.toInt()) // Orange (Diproses)
        }

        if (isHistory) {
            btnDetail.visibility = View.GONE // Hilangkan tombol update jika sudah selesai
        } else {
            btnDetail.text = "Update Status"
            btnDetail.setOnClickListener {
                showUpdateStatusDialog(order)
            }
        }

        container.addView(cardView)
    }

    private fun showUpdateStatusDialog(order: OrderModel) {
        val options = arrayOf("Diproses", "Dikerjakan", "Selesai", "Dibatalkan")
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Update Status: ${order.serviceName}")
        builder.setItems(options) { dialog, which ->
            val selectedStatus = options[which]
            updateOrderStatus(order.id, selectedStatus)
        }
        builder.show()
    }

    private fun updateOrderStatus(orderId: String, newStatus: String) {
        // Tidak perlu panggil listenToOrders() lagi karena otomatis update via SnapshotListener
        db.collection("orders").document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed update", Toast.LENGTH_SHORT).show()
            }
    }
}