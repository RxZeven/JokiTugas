package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityOrderDetailBinding

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil Data dari Intent
        val serviceName = intent.getStringExtra("EXTRA_SERVICE") ?: "-"
        val deadline = intent.getStringExtra("EXTRA_DEADLINE") ?: "-"
        val price = intent.getStringExtra("EXTRA_PRICE") ?: "-"
        val status = intent.getStringExtra("EXTRA_STATUS") ?: "-"

        // Data Dummy untuk specs (karena di list aktivitas tadi kita blm simpan detail speknya)
        // Nanti bisa diambil dari object OrderModel kalau sudah lengkap
        val specs = "• Fitur Lengkap\n• Garansi Revisi\n• Termasuk Source Code"

        // 2. Set Data ke UI
        binding.tvDetailServiceName.text = serviceName
        binding.tvDetailDeadline.text = deadline
        binding.tvDetailPrice.text = price
        binding.tvDetailStatus.text = status
        binding.tvDetailSpecs.text = specs

        setupListeners()
    }

    private fun setupListeners() {
        // Back Button
        binding.btnBack.setOnClickListener {
            finish() // Kembali ke AktivitasActivity
        }
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
            startActivity(Intent(this, AktivitasActivity::class.java))
        }

        binding.menuChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}