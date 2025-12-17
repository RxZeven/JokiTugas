package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityAktivitasBinding

class AktivitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAktivitasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAktivitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    // Gunakan onResume agar setiap kali halaman ini dibuka, data direfresh
    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun loadOrders() {
        // 1. Bersihkan wadah dulu agar tidak duplikat
        binding.containerOrders.removeAllViews()

        // 2. Cek apakah ada data di DummyData
        val orders = DummyData.orderHistory

        if (orders.isEmpty()) {
            // Tampilkan teks kosong
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.containerOrders.visibility = View.GONE
        } else {
            // Sembunyikan teks kosong
            binding.tvEmptyState.visibility = View.GONE
            binding.containerOrders.visibility = View.VISIBLE

            // 3. Loop data dari 'Database' dan tampilkan
            // Kita balik urutannya (reversed) agar pesanan terbaru muncul paling atas
            orders.reversed().forEach { order ->
                addOrderCard(order)
            }
        }
    }

    private fun addOrderCard(order: OrderModel) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_order_card, binding.containerOrders, false)

        val tvName = cardView.findViewById<TextView>(R.id.tvServiceName)
        val tvDate = cardView.findViewById<TextView>(R.id.tvDate)
        val tvPrice = cardView.findViewById<TextView>(R.id.tvPrice) // View Baru
        val tvStatus = cardView.findViewById<TextView>(R.id.tvStatus)
        val btnDetail = cardView.findViewById<TextView>(R.id.btnDetail)

        // Set Data
        tvName.text = order.serviceName
        tvDate.text = order.deadline
        tvPrice.text = order.price // Menampilkan Harga!
        tvStatus.text = order.status

        btnDetail.setOnClickListener {
            val intent = Intent(this, OrderDetailActivity::class.java)

            // Kirim Data ke Halaman Detail
            intent.putExtra("EXTRA_SERVICE", order.serviceName)
            intent.putExtra("EXTRA_DEADLINE", order.deadline)
            intent.putExtra("EXTRA_PRICE", order.price)
            intent.putExtra("EXTRA_STATUS", order.status)

            startActivity(intent)
        }

        binding.containerOrders.addView(cardView)
    }

    private fun setupListeners() {
        // ... (Kode Listener Nav Bar sama seperti sebelumnya) ...
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