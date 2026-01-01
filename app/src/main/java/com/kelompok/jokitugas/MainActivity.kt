package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Klik Profil
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }

        // Klik Tombol Promo
        binding.cardPromo.setOnClickListener {
            startActivity(Intent(this, PromoDetailActivity::class.java))
            overridePendingTransition(0, 0)
        }

        // Klik Tombol Detail Pesanan Joki
        binding.btnDetailJoki.setOnClickListener {
            val intent = Intent (this, JokiOrderActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        // --- Bottom Navigation Logic ---

        binding.menuActivity.setOnClickListener {
           val intent = Intent(this, AktivitasActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

//        binding.menuHome.setOnClickListener {
//            Toast.makeText(this, "Tab: Beranda (Current)", Toast.LENGTH_SHORT).show()
//        }

        binding.menuChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}