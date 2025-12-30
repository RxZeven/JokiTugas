package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityOrderDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityOrderDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        val orderId = intent.getStringExtra("EXTRA_ORDER_ID")

        if (orderId == null) {
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        loadOrderDetail(orderId)
    }

    private fun setupListeners() {
        // Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Profile Button
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Chat Button
        binding.btnChatJoki.setOnClickListener {
            val intent = Intent(this, ChatRoomActivity::class.java)
            startActivity(intent)
        }

        // Bottom Nav Logic
        binding.menuHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.menuActivity.setOnClickListener {
            // Sudah di halaman yang relevan (konteks aktivitas), refresh atau stay
            startActivity(Intent(this, AktivitasActivity::class.java))
            finish()
        }

        binding.menuChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun loadOrderDetail(orderId: String) {
        db.collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // 1. Data Utama
                    binding.tvDetailServiceName.text = doc.getString("serviceTitle")
                    binding.tvDetailDeadline.text = doc.getString("deadline")
                    binding.tvDetailPrice.text = doc.getString("price")
                    binding.tvDetailStatus.text = doc.getString("status")

                    // 2. Spesifikasi (Specs)
                    val specs = doc.getString("specs")
                    if (!specs.isNullOrEmpty()) {
                        binding.tvDetailSpecs.text = specs
                    } else {
                        binding.tvDetailSpecs.text = "• Fitur Lengkap\n• Garansi Revisi\n• Termasuk Source Code"
                    }

                    // 3. Catatan (Notes)
                    val notes = doc.getString("notes")
                    if (!notes.isNullOrEmpty()) {
                        binding.tvDetailNotes.text = notes
                    } else {
                        binding.tvDetailNotes.text = "-"
                    }

                    // 4. Metode Pembayaran
                    val paymentMethod = doc.getString("paymentMethod")
                    if (!paymentMethod.isNullOrEmpty()) {
                        binding.tvDetailPaymentMethod.text = paymentMethod
                    } else {
                        binding.tvDetailPaymentMethod.text = "QRIS / Transfer"
                    }

                    // 5. Tanggal Pembayaran
                    val timestamp = doc.getLong("createdAt")
                    if (timestamp != null) {
                        val date = Date(timestamp)
                        // Format contoh: 21 Des 2025, 15:30
                        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                        binding.tvDetailDate.text = format.format(date)
                    } else {
                        binding.tvDetailDate.text = "-"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat detail pesanan", Toast.LENGTH_SHORT).show()
            }
    }
}
