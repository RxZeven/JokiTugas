package com.kelompok.jokitugas

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kelompok.jokitugas.databinding.ActivityChatRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    
    private var chatId: String = ""
    private var otherName: String = "Admin"
    private var currentUserName: String = "User"
    private var listenerRegistration: ListenerRegistration? = null
    
    // AI Chatbot
    // Mengganti model ke gemini-pro agar lebih stabil
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro", 
        apiKey = "AIzaSyDU6Wj_ZuCRevahzBOhxnzl_g83H9mq9UI"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        otherName = intent.getStringExtra("EXTRA_NAME") ?: "Admin"
        val explicitChatId = intent.getStringExtra("EXTRA_CHAT_ID")
        
        binding.tvJockeyName.text = otherName
        
        if (explicitChatId != null) {
            chatId = explicitChatId
            listenToMessages()
        } else {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                chatId = "chat_$uid"
                listenToMessages()
                fetchCurrentUserName()
            }
        }

        setupUI()
    }
    
    private fun fetchCurrentUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener {
            currentUserName = it.getString("name") ?: "User"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { 
            finish()
            overridePendingTransition(0, 0)
        }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message, "text")
                binding.etMessage.setText("")
                
                replyWithAI(message)
            }
        }

        binding.btnAddPhoto.setOnClickListener {
            Toast.makeText(this, "Fitur upload gambar dinonaktifkan sementara.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun replyWithAI(userMessage: String) {
        lifecycleScope.launch {
            try {
                val prompt = "Kamu adalah Admin Joki Tugas yang ramah dan profesional bernama Kang Coding. " +
                        "Jawablah pertanyaan user ini dengan singkat dan membantu: \"$userMessage\""
                
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }
                val aiReply = response.text ?: "Maaf, saya sedang sibuk. Mohon tunggu sebentar."
                
                sendAdminMessage(aiReply)
                
            } catch (e: Exception) {
                e.printStackTrace()
                // Menyederhanakan pesan error untuk user
                val errorMessage = if (e.message?.contains("404") == true) {
                    "Maaf, layanan AI sedang gangguan. (Model not found)"
                } else {
                    "Maaf, saya sedang tidak bisa menjawab saat ini."
                }
                sendAdminMessage(errorMessage)
            }
        }
    }
    
    private fun sendAdminMessage(message: String) {
        val messageMap = hashMapOf(
            "senderId" to "admin_bot",
            "text" to message,
            "type" to "text",
            "timestamp" to System.currentTimeMillis() + 100
        )
        
        db.collection("chats").document(chatId).collection("messages")
            .add(messageMap)
            
         val chatMeta = hashMapOf(
            "lastMessage" to message,
            "lastTime" to System.currentTimeMillis(),
            "participants" to listOf(auth.currentUser?.uid, "admin"),
            "userName" to currentUserName 
        )
        db.collection("chats").document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge())
    }

    private fun sendMessage(content: String, type: String) {
        val uid = auth.currentUser?.uid ?: return
        
        val messageMap = hashMapOf(
            "senderId" to uid,
            "text" to content,
            "type" to type,
            "timestamp" to System.currentTimeMillis()
        )
        
        db.collection("chats").document(chatId).collection("messages")
            .add(messageMap)
            
        val lastMsgPreview = if (type == "image") "📷 [Gambar]" else content
        
        val chatMeta = hashMapOf(
            "lastMessage" to lastMsgPreview,
            "lastTime" to System.currentTimeMillis(),
            "participants" to listOf(uid, "admin"),
            "userName" to currentUserName 
        )
        
        db.collection("chats").document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge()) 
    }

    private fun listenToMessages() {
        binding.containerChat.removeAllViews()
    
        listenerRegistration = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null) {
                    binding.containerChat.removeAllViews()
                    val currentUid = auth.currentUser?.uid
                    
                    for (doc in snapshots) {
                        val content = doc.getString("text") ?: ""
                        val sender = doc.getString("senderId") ?: ""
                        val type = doc.getString("type") ?: "text"
                        
                        val isMe = (sender == currentUid)
                        
                        if (type == "image") {
                            addChatBubble("[Gambar]", isMe)
                        } else {
                            addChatBubble(content, isMe)
                        }
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
