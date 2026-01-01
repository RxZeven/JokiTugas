package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityJokiOrderBinding
import com.kelompok.jokitugas.databinding.ItemJokiExpandableBinding
import com.google.firebase.auth.FirebaseAuth


class JokiOrderActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityJokiOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJokiOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }


        setupUI()
    }

    private fun setupUI() {
        // 1. Tombol Back dan Profile
        binding.btnBack.setOnClickListener {
            finish() // Kembali ke MainActivity
            overridePendingTransition(0, 0)
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }

        // 2. Setup Nav Bar (Hanya Dummy Klik)
        binding.menuHome.setOnClickListener {
            finish() // Balik ke Home
            overridePendingTransition(0, 0)
        }
        binding.menuChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
            overridePendingTransition(0, 0)
        }
        binding.menuActivity.setOnClickListener {
            startActivity(Intent(this, AktivitasActivity::class.java))
            overridePendingTransition(0, 0)
        }

        // 3. Setup List Item (Coding, Makalah, dll)
        // Kita panggil fungsi setupItem untuk setiap 'include' yang ada di XML

        val services = listOf(
            JokiService(
                binding.itemCoding,
                "Coding",
                "Python\nC++\nAplikasi Mobile (Android)\nWeb\nRoblox (Luau)"
            ),
            JokiService(
                binding.itemMakalah,
                "Makalah",
                "Makalah Ilmiah\nEsai Argumentatif\nResume\nArtikel Populer"
            ),
            JokiService(
                binding.itemPPT,
                "PowerPoint (PPT)",
                "PPT dari Materi\nRedesign\nTemplate"
            ),
            JokiService(
                binding.itemJurnal,
                "Review Jurnal",
                "Review Jurnal Internasional\nCritical Review\nMatrik Jurnal\nResume Jurnal"
            ),
            JokiService(
                binding.itemParafrase,
                "Parafrase & Cek Turnitin",
                "Parafrase Human-Made\nParafrase AI-Editing\nPenurunan Skor Turnitin"
            ),
            JokiService(
                binding.itemSkripsi,
                "Skripsi",
                "Pencarian Judul & Masalah\nPembuatan Proposal (Bab 1-3)\nOlah Data Statistik\nRevisi Pasca Sidang\nFormat Layouting"
            )
        )

        // 2. Kita lakukan Perulangan (Looping)
        // Code ini akan otomatis menjalankan setupExpandableItem untuk setiap item di list
        services.forEach { service ->
            setupExpandableItem(service.binding, service.title, service.desc)
        }
    }

    // Fungsi Pintar untuk Mengatur Dropdown
    private fun setupExpandableItem(itemBinding: ItemJokiExpandableBinding, title: String, description: String) {
        // Set Teks Judul & Deskripsi
        itemBinding.tvTitle.text = title
        itemBinding.tvDesc.text = description

        // Klik Header untuk Buka/Tutup
        itemBinding.layoutHeader.setOnClickListener {
            val body = itemBinding.layoutBody
            val arrow = itemBinding.ivArrow

            if (body.visibility == View.GONE) {
                // Saat MEMBUKA
                body.visibility = View.VISIBLE
                arrow.animate().rotation(180f).setDuration(300).start() // Putar panah ke atas
            } else {
                // Saat MENUTUP
                body.visibility = View.GONE
                arrow.animate().rotation(0f).setDuration(300).start() // Putar panah ke bawah
            }
        }

        // Klik Tombol Beli
        itemBinding.btnBuy.setOnClickListener {
            // Intent untuk pindah ke DetailOrderActivity
            val intent = Intent(this, DetailOrderActivity::class.java)

            // TITIP DATA (Passing Data)
            intent.putExtra("EXTRA_TITLE", title)
            intent.putExtra("EXTRA_DESC", description)
            intent.putExtra("EXTRA_USER_ID", auth.currentUser?.uid)


            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}

// Wadah data sederhana
data class JokiService(
    val binding: ItemJokiExpandableBinding, // View layout-nya
    val title: String,                      // Judul
    val desc: String                        // Deskripsi
)