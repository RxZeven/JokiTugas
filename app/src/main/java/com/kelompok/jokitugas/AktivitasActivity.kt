package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityAktivitasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AktivitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAktivitasBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAktivitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Kita panggil loadOrdersFromFirebase alih-alih local DummyData
        loadOrdersFromFirebase()
    }

    private fun loadOrdersFromFirebase() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            // User belum login
            binding.tvEmptyState.text = "Silakan login untuk melihat pesanan"
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.containerOrders.visibility = View.GONE
            return
        }

        binding.containerOrders.removeAllViews()

        // Ambil data dari Firestore
        db.collection("orders")
            .whereEqualTo("userId", userId) // Hanya ambil pesanan milik user ini
            // Jika ingin urut berdasarkan tanggal, pastikan createdAt ada
            // .orderBy("createdAt", Query.Direction.DESCENDING) 
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.containerOrders.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.containerOrders.visibility = View.VISIBLE

                    for (document in documents) {
                        val serviceName = document.getString("serviceTitle") ?: "Layanan"
                        val specs = document.getString("specs") ?: "" // Ambil detail specs juga
                        
                        // Gabungkan Nama Layanan + Sedikit Detail (biar tidak bingung)
                        // Contoh: "Joki Coding (Python, Web)"
                        val displayName = if (specs.isNotEmpty()) {
                             // Ambil kata pertama dari specs sebagai hint, atau tampilkan specs singkat
                             // Misalnya specs: "Python, Web, Machine Learning"
                             // Kita ambil "Python..."
                             val shortSpec = specs.split(",")[0].trim()
                             "$serviceName ($shortSpec)"
                        } else {
                            serviceName
                        }

                        val order = OrderModel(
                            id = document.getString("id") ?: document.id,
                            serviceName = displayName, // Pakai nama yang sudah digabung
                            deadline = document.getString("deadline") ?: "-",
                            price = document.getString("price") ?: "-",
                            status = document.getString("status") ?: "Diproses"
                        )
                        addOrderCard(order)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal memuat data: ${exception.message}", Toast.LENGTH_SHORT).show()
                binding.tvEmptyState.text = "Gagal memuat data."
                binding.tvEmptyState.visibility = View.VISIBLE
            }
    }

    private fun addOrderCard(order: OrderModel) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_order_card, binding.containerOrders, false)

        val tvName = cardView.findViewById<TextView>(R.id.tvServiceName)
        val tvDate = cardView.findViewById<TextView>(R.id.tvDeadline)
        val tvPrice = cardView.findViewById<TextView>(R.id.tvPrice)
        val tvStatus = cardView.findViewById<TextView>(R.id.tvStatus)
        val btnDetail = cardView.findViewById<TextView>(R.id.btnDetail)

        tvName.text = order.serviceName
        tvDate.text = order.deadline
        tvPrice.text = order.price
        tvStatus.text = order.status

        btnDetail.setOnClickListener {
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("EXTRA_ORDER_ID", order.id)
            startActivity(intent)
        }

        binding.containerOrders.addView(cardView)
    }


    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.menuHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        binding.menuActivity.setOnClickListener {
            startActivity(Intent(this, AktivitasActivity::class.java))
        }

        binding.menuChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}