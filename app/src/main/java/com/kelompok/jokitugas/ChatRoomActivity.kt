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
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.kelompok.jokitugas.databinding.ActivityChatRoomBinding
import kotlinx.coroutines.launch

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding

    // 1. Inisialisasi Model AI (Gemini)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = "AIzaSyAwVymNGzryjxL9HYrLxKbxfO8d_AoQA1g"
    )

    // 2. Simpan Riwayat Chat (Supaya AI nyambung kalau diajak ngobrol panjang)
    private val chatHistory = StringBuilder()

    // 1. LAUNCHER GALERI (Cara Modern)
    // Ini fungsi yang akan dipanggil SETELAH user memilih foto
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // 'uri' adalah alamat file gambar yang dipilih user

            // Tampilkan gambar tersebut di Chat Bubble sebagai "Saya"
            addImageBubble(uri, isMe = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Persona Awal (System Prompt)
        // Kita "cuci otak" AI-nya dulu biar berlagak jadi Penjoki
        chatHistory.append("Instruksi: Kamu adalah admin jasa joki tugas bernama 'Kang Coding'. " +
                "Gaya bicaramu santai, ramah, suka membantu, dan singkat padat. " +
                "Kamu menawarkan jasa coding, makalah, powerpoint, riview jurnal, parafrase & cek turnitin, dan skripsi. " +
                "Jangan pernah bilang kamu adalah AI atau robot. Anggap kamu manusia.\n\n")

        setupUI()
    }

    private fun setupUI() {
        // 1. Tombol Back ke Home (MainActivity)
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 2. Tombol Kirim Pesan
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                // Tampilkan Chat Kita (Kanan)
                addChatBubble(message, isMe = true)
                binding.etMessage.setText("")

                // Tambahkan ke riwayat chat
                chatHistory.append("User: $message\n")

                // 2. Panggil AI Gemini (Tampilkan efek 'Sedang mengetik...')
                showTypingIndicator(true) // (Opsional: Kita buat simple dulu)

                // Panggil AI di Background Thread
                lifecycleScope.launch {
                    try {
                        // Kirim seluruh riwayat chat ke AI agar konteksnya nyambung
                        val prompt = chatHistory.toString() + "Kang Coding:"

                        val response = generativeModel.generateContent(prompt)
                        val aiReply = response.text ?: "Maaf kak, sinyal lagi jelek nih. Coba lagi ya."

                        // Tambahkan balasan AI ke riwayat
                        chatHistory.append("Kang Coding: $aiReply\n")

                        // 3. Tampilkan Balasan AI di UI
                        showTypingIndicator(false) // Hilangkan loading
                        addChatBubble(aiReply, isMe = false)

                    } catch (e: Exception) {
                        showTypingIndicator(false)
                        addChatBubble("Error: ${e.localizedMessage}", isMe = false)
                    }
                }
            }
        }

        // 3. Tombol Tambah Foto
        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 4. Nav Bar Logic
        binding.menuHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.menuActivity.setOnClickListener {
            startActivity(Intent(this, AktivitasActivity::class.java))
            finish()
        }
        binding.menuChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }
    }

    // Fungsi helper untuk menampilkan status "Sedang mengetik..." (Opsional)
    private fun showTypingIndicator(show: Boolean) {
        if (show) {
            binding.headerContainer.findViewById<TextView>(R.id.tvJockeyName).text = "Kang Coding (Sedang mengetik...)"
        } else {
            binding.headerContainer.findViewById<TextView>(R.id.tvJockeyName).text = "Kang Coding (Admin)"
        }
    }

    // Fungsi Pintar Membuat Bubble Chat
    private fun addChatBubble(message: String, isMe: Boolean) {
        val textView = TextView(this)
        textView.text = message
        textView.textSize = 16f
        textView.setPadding(32, 24, 32, 24)

        // Atur Layout Parameters (Margin & Gravity)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 16 // Jarak antar chat

        if (isMe) {
            // Setting Chat KITA (Kanan)
            params.gravity = Gravity.END // Rata Kanan
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            textView.setBackgroundResource(R.drawable.bg_chat_me) // Background Hijau
        } else {
            // Setting Chat LAWAN (Kiri)
            params.gravity = Gravity.START // Rata Kiri
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
            textView.setBackgroundResource(R.drawable.bg_chat_other) // Background Putih
        }

        textView.layoutParams = params

        // Batasi lebar chat agar tidak full layar (maks 70% layar)
        textView.maxWidth = (resources.displayMetrics.widthPixels * 0.75).toInt()

        // Tambahkan ke Container
        binding.containerChat.addView(textView)

        // Auto Scroll ke Bawah
        binding.scrollViewChat.post {
            binding.scrollViewChat.fullScroll(View.FOCUS_DOWN)
        }
    }

    // Fungsi Baru: Menampilkan Bubble Gambar
    private fun addImageBubble(imageUri: android.net.Uri, isMe: Boolean) {
        val cardView = androidx.cardview.widget.CardView(this)

        // Setting Layout Params untuk CardView (Wadah Gambar)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 16

        // Atur Posisi Kiri/Kanan
        if (isMe) {
            params.gravity = Gravity.END // Kanan
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_primary)) // Frame Hijau
        } else {
            params.gravity = Gravity.START // Kiri
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        cardView.layoutParams = params
        cardView.radius = 24f // Sudut melengkung
        cardView.cardElevation = 0f

        // Tambahkan Padding tipis antara Card dan Gambar
        cardView.setContentPadding(4, 4, 4, 4)

        // Buat ImageView secara program
        val imageView = android.widget.ImageView(this)
        imageView.setImageURI(imageUri) // Load gambar dari URI
        imageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        imageView.adjustViewBounds = true

        // Batasi ukuran gambar agar tidak memenuhi layar (Maks 250dp)
        val sizeInPx = (250 * resources.displayMetrics.density).toInt()
        val imgParams = android.widget.FrameLayout.LayoutParams(sizeInPx, sizeInPx)
        imageView.layoutParams = imgParams

        // Masukkan Image ke Card
        cardView.addView(imageView)

        // Masukkan Card ke Container Chat
        binding.containerChat.addView(cardView)

        // Scroll ke bawah
        binding.scrollViewChat.post {
            binding.scrollViewChat.fullScroll(View.FOCUS_DOWN)
        }
    }
}