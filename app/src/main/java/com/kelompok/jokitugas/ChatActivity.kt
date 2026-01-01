package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.kelompok.jokitugas.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupListeners()
        loadChatRooms()
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
        
        // FAB untuk New Chat (Manual trigger chat dengan Admin)
        binding.fabNewChat.setOnClickListener {
             // Langsung buka chat room umum (Admin Utama)
             val intent = Intent(this, ChatRoomActivity::class.java)
             intent.putExtra("EXTRA_CHAT_ID", "admin_chat") 
             intent.putExtra("EXTRA_NAME", "Admin Utama")
             startActivity(intent)
        }
    }

    private fun loadChatRooms() {
        binding.containerChatHistory.removeAllViews()
        val currentUserId = auth.currentUser?.uid ?: return

        // Query cari room chat di mana user ini terlibat
        // Struktur Firestore ideal: collection("chat_rooms").whereArrayContains("participants", uid)
        // Tapi untuk simpelnya, kita buat 1 room default Admin dulu.

        // Tampilkan 1 Chat Default: Admin Support
        addChatHistory("Admin Joki Tugas", "Halo, ada yang bisa dibantu?", "Sekarang", 0, "admin_chat")
    }

    private fun addChatHistory(name: String, message: String, time: String, unreadCount: Int, chatId: String) {
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
            tvMessage.setTextColor(resources.getColor(R.color.app_primary, null))
        } else {
            badgeContainer.visibility = View.GONE
        }

        // Klik Item -> Buka Room Chat Real
        itemView.setOnClickListener {
            val intent = Intent(this, ChatRoomActivity::class.java)
            intent.putExtra("EXTRA_CHAT_ID", chatId) 
            intent.putExtra("EXTRA_NAME", name)
            startActivity(intent)
        }

        binding.containerChatHistory.addView(itemView)
    }
}