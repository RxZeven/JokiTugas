package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kelompok.jokitugas.databinding.ActivityChatRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Date

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    
    private var chatId: String = ""
    private var otherName: String = "Admin"
    private var listenerRegistration: ListenerRegistration? = null

    // 1. LAUNCHER GALERI
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Toast.makeText(this, "Fitur kirim gambar sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
            // addImageBubble(uri, isMe = true) // Nanti aktifkan kalau sudah ada Storage
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        // Ambil Data dari Intent
        otherName = intent.getStringExtra("EXTRA_NAME") ?: "Admin"
        val explicitChatId = intent.getStringExtra("EXTRA_CHAT_ID")
        
        // Set Nama di Header
        binding.tvJockeyName.text = otherName
        
        // Tentukan Chat ID
        if (explicitChatId != null) {
            chatId = explicitChatId
            listenToMessages()
        } else {
            // Kalau tidak ada ID, cari atau buat chat baru (Logic sementara: pakai ID User sendiri sebagai 'Admin Chat')
            val uid = auth.currentUser?.uid
            if (uid != null) {
                chatId = "chat_$uid" // Unik per user
                listenToMessages()
            }
        }

        setupUI()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun setupUI() {
        // Back Button
        binding.btnBack.setOnClickListener { finish() }

        // Tombol Kirim Pesan
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.setText("")
            }
        }

        // Tombol Tambah Foto
        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }
    
    private fun sendMessage(messageText: String) {
        val uid = auth.currentUser?.uid ?: return
        
        val messageMap = hashMapOf(
            "senderId" to uid,
            "text" to messageText,
            "timestamp" to System.currentTimeMillis()
        )
        
        // Simpan ke Sub-Collection "messages" di dalam Dokumen Chat Room
        db.collection("chats").document(chatId).collection("messages")
            .add(messageMap)
            .addOnFailureListener {
                Toast.makeText(this, "Gagal kirim pesan", Toast.LENGTH_SHORT).show()
            }
            
        // Update "Last Message" di Dokumen Induk (Agar list chat terupdate)
        val chatMeta = hashMapOf(
            "lastMessage" to messageText,
            "lastTime" to System.currentTimeMillis(),
            "participants" to listOf(uid, "admin") // Contoh sederhana
        )
        db.collection("chats").document(chatId).set(chatMeta) // Gunakan set/update
    }

    private fun listenToMessages() {
        // Hapus chat dummy bawaan layout
        binding.containerChat.removeAllViews()
    
        // Dengar pesan secara Real-time
        listenerRegistration = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    binding.containerChat.removeAllViews()
                    val currentUid = auth.currentUser?.uid
                    
                    for (doc in snapshots) {
                        val text = doc.getString("text") ?: ""
                        val sender = doc.getString("senderId") ?: ""
                        
                        val isMe = (sender == currentUid)
                        addChatBubble(text, isMe)
                    }
                }
            }
    }

    private fun addChatBubble(message: String, isMe: Boolean) {
        val textView = TextView(this)
        textView.text = message
        textView.textSize = 16f
        textView.setPadding(32, 24, 32, 24)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 16 

        if (isMe) {
            params.gravity = Gravity.END 
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            textView.setBackgroundResource(R.drawable.bg_chat_me) 
        } else {
            params.gravity = Gravity.START 
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
            textView.setBackgroundResource(R.drawable.bg_chat_other) 
        }

        textView.layoutParams = params
        textView.maxWidth = (resources.displayMetrics.widthPixels * 0.75).toInt()

        binding.containerChat.addView(textView)

        binding.scrollViewChat.post {
            binding.scrollViewChat.fullScroll(View.FOCUS_DOWN)
        }
    }
}