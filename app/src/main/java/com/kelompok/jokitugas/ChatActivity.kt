package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.kelompok.jokitugas.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()

        // Tambahkan Data Dummy
        addChatHistory("Kang Coding", "Siap kak, sedang dikerjakan ya.", "10:30", 2)
        addChatHistory("Jasa Makalah Express", "File bab 1 sudah saya kirim ke email.", "Kemarin", 0)
        addChatHistory("Admin Skripsi", "Halo, untuk revisi bab 4 bagaimana?", "08/12", 5)
        addChatHistory("Customer Service", "Selamat datang di aplikasi Joki App!", "01/12", 0)
    }

    private fun setupListeners() {
        // Back -> MainActivity
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Navigasi
        binding.menuHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.menuActivity.setOnClickListener {
            startActivity(Intent(this, AktivitasActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        // menuChat tidak perlu listener karena kita sedang di halaman Chat
    }

    private fun addChatHistory(name: String, message: String, time: String, unreadCount: Int) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_chat_history, binding.containerChatHistory, false)

        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val tvMessage = itemView.findViewById<TextView>(R.id.tvLastMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val badgeContainer = itemView.findViewById<CardView>(R.id.badgeContainer)
        val tvCount = itemView.findViewById<TextView>(R.id.tvUnreadCount)

        // Set Data
        tvName.text = name
        tvMessage.text = message
        tvTime.text = time

        // Logic Badge Unread
        if (unreadCount > 0) {
            badgeContainer.visibility = View.VISIBLE
            tvCount.text = unreadCount.toString()
            // Bold nama dan pesan kalau belum dibaca
            tvMessage.setTextColor(resources.getColor(R.color.app_primary, null))
        } else {
            badgeContainer.visibility = View.GONE
        }

        // Klik Item -> Buka Room Chat
        itemView.setOnClickListener {
            val intent = Intent(this, ChatRoomActivity::class.java)
            // Nanti bisa kirim nama penjoki lewat intent biar dinamis
            // intent.putExtra("EXTRA_NAME", name)
            startActivity(intent)
        }

        binding.containerChatHistory.addView(itemView)
    }
}