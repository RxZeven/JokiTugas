package com.kelompok.jokitugas

import android.content.Intent
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.bumptech.glide.Glide

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    
    private var chatId: String = ""
    private var otherName: String = "Admin"
    private var currentUserName: String = "User" // Tambahan untuk simpan nama
    private var listenerRegistration: ListenerRegistration? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            uploadImageToStorage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        
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
                fetchCurrentUserName() // Ambil nama user sendiri buat disimpan
            }
        }

        setupUI()
    }
    
    // Ambil nama user dari database users buat disave ke chat room
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
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message, "text")
                binding.etMessage.setText("")
            }
        }

        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }
    
    private fun uploadImageToStorage(fileUri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val ref = storage.reference.child("chat_images/$fileName")

        Toast.makeText(this, "Mengunggah gambar...", Toast.LENGTH_SHORT).show()

        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    sendMessage(imageUrl, "image")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal upload gambar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
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
            
        // UPDATE METADATA CHAT (Penting untuk Admin List)
        val lastMsgPreview = if (type == "image") "📷 [Gambar]" else content
        
        // Kita simpan userName juga biar Admin tau ini chat dari siapa
        val chatMeta = hashMapOf(
            "lastMessage" to lastMsgPreview,
            "lastTime" to System.currentTimeMillis(),
            "participants" to listOf(uid, "admin"),
            "userName" to currentUserName // <-- INI YANG PENTING
        )
        
        // Gunakan set dengan Merge agar tidak menimpa data lain kalau ada
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
                            addImageBubble(content, isMe)
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
    
    private fun addImageBubble(imageUrl: String, isMe: Boolean) {
        val cardView = androidx.cardview.widget.CardView(this)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 16

        if (isMe) {
            params.gravity = Gravity.END 
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_primary)) 
        } else {
            params.gravity = Gravity.START 
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        cardView.layoutParams = params
        cardView.radius = 24f 
        cardView.cardElevation = 0f
        cardView.setContentPadding(4, 4, 4, 4)

        val imageView = android.widget.ImageView(this)
        
        Glide.with(this)
             .load(imageUrl)
             .centerCrop()
             .into(imageView)

        val sizeInPx = (200 * resources.displayMetrics.density).toInt()
        val imgParams = android.widget.FrameLayout.LayoutParams(sizeInPx, sizeInPx)
        imageView.layoutParams = imgParams

        cardView.addView(imageView)
        binding.containerChat.addView(cardView)

        binding.scrollViewChat.post {
            binding.scrollViewChat.fullScroll(View.FOCUS_DOWN)
        }
    }
}