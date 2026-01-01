package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.kelompok.jokitugas.databinding.ActivityAdminChatListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminChatListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupUI()
        loadChatRooms()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadChatRooms() {
        // Ambil semua dokumen di koleksi "chats"
        // Di aplikasi riil, mungkin perlu filter "participants" array-contains "admin"
        db.collection("chats")
            .orderBy("lastTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Gagal memuat chat: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    binding.containerChatList.removeAllViews()
                    
                    if (snapshots.isEmpty) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        for (doc in snapshots) {
                            val chatId = doc.id
                            // Ambil nama user dari metadata yang baru kita update di ChatRoomActivity
                            val userName = doc.getString("userName") ?: "User (Tanpa Nama)"
                            val lastMessage = doc.getString("lastMessage") ?: "..."
                            val lastTime = doc.getLong("lastTime") ?: 0L

                            addChatItem(chatId, userName, lastMessage, lastTime)
                        }
                    }
                }
            }
    }

    private fun addChatItem(chatId: String, name: String, message: String, timestamp: Long) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_chat_history, binding.containerChatList, false)

        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val tvMessage = itemView.findViewById<TextView>(R.id.tvLastMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val badgeContainer = itemView.findViewById<CardView>(R.id.badgeContainer)

        tvName.text = name
        tvMessage.text = message
        
        // Format Waktu (Contoh: 10:30 atau Kemarin)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvTime.text = sdf.format(Date(timestamp))

        // Sembunyikan badge unread dulu (Logic unread butuh sub-collection query yang lebih kompleks)
        badgeContainer.visibility = View.GONE

        itemView.setOnClickListener {
            // Admin membalas -> Buka Chat Room
            val intent = Intent(this, ChatRoomActivity::class.java)
            intent.putExtra("EXTRA_CHAT_ID", chatId)
            intent.putExtra("EXTRA_NAME", name) // Tampilkan nama user di header admin
            startActivity(intent)
        }

        binding.containerChatList.addView(itemView)
    }
}