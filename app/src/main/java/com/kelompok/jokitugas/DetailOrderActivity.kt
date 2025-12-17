package com.kelompok.jokitugas

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.kelompok.jokitugas.databinding.ActivityDetailOrderBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailOrderBinding
    private val calendar = Calendar.getInstance() // Untuk menyimpan tanggal pilihan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil Nama Layanan dari Halaman Sebelumnya
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Layanan"
        binding.tvServiceTitle.text = title

        // 2. Setup Checkbox Dinamis Berdasarkan Judul Layanan
        setupDynamicOptions(title)

        // 3. Setup Listener Lainnya
        setupListeners()
    }

    private fun setupDynamicOptions(serviceName: String) {
        // --- DATA OPSI ---
        // Tentukan daftar opsi berdasarkan nama layanan
        val options = when (serviceName) {
            "Coding" -> listOf( "C++", "Python", "Aplikasi Mobile (Android)", "Web", "Roblox (Luau)")
            "Makalah" -> listOf("Makalah Ilmiah", "Esai Argumentasi", "Resume", "Artikel Populer")
            "PowerPoint (PPT)" -> listOf("PPT dari Materi", "Redesign", "Template")
            "Review Jurnal" -> listOf("Review Jurnal Internasional", "Critical Review", "Matrik Jurnal", "Resume Jurnal")
            "Parafrase & Cek Turnitin" -> listOf("Parafrase Human-Made", "Parafrase AI-Editing", "Penurunan Skor Turnitin")
            "Skripsi" -> listOf("Pencarian Judul & Masalah", "Pembuatan Proposal (Bab 1-3)", "Olah Data Statistik", "Revisi Pasca Sidang", "Format Layouting")
            else -> listOf("") // Default jika nama tidak dikenali
        }

        // --- BUAT CHECKBOX SECARA PROGRAMMATIC ---
        // Kita loop setiap opsi dan buatkan checkbox-nya
        for (optionText in options) {
            val checkBox = CheckBox(this)
            checkBox.text = optionText
            checkBox.textSize = 16f
            checkBox.setTextColor(Color.parseColor("#1E293B")) // Warna Teks (Sesuai tema anda)

            // Ubah warna kotak centang jadi Hijau Army (App Primary)
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#1D4ED8"))
            checkBox.buttonTintList = colorStateList

            // Masukkan Checkbox ke dalam Wadah (LinearLayout) di XML
            binding.containerOptions.addView(checkBox)
        }
    }

    private fun setupListeners() {
        // Tombol Back
        binding.btnBack.setOnClickListener {
            finish() }

        // Tombol Pofile
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Nav Bar
        // Beranda
        binding.menuHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        // Aktivitas
        binding.menuActivity.setOnClickListener {
            startActivity(Intent(this, AktivitasActivity::class.java))
        }
        // chat
        binding.menuChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // Logic DATE PICKER
        binding.etDeadline.setOnClickListener {
            showDatePicker()
        }
        binding.tilDeadline.setEndIconOnClickListener {
            showDatePicker()
        }

        // Tombol Order
        binding.btnOrderNow.setOnClickListener {
            val deadline = binding.etDeadline.text.toString()
            val notes = binding.etNotes.text.toString()

            // AMBIL DATA CHECKBOX YANG DIPILIH
            val selectedOptions = mutableListOf<String>()

            // Cek satu per satu anak (child) yang ada di dalam containerOptions
            binding.containerOptions.children.forEach { view ->
                if (view is CheckBox && view.isChecked) {
                    selectedOptions.add(view.text.toString())
                }
            }

            if (deadline.isEmpty()) {
                binding.tilDeadline.error = "Mohon pilih deadline!"
                return@setOnClickListener
            }

            // Jika tidak ada checkbox yang dipilih (Optional, boleh dihapus kalau tidak wajib)
            if (selectedOptions.isEmpty()) {
                Toast.makeText(this, "Mohon pilih minimal satu spesifikasi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Format Hasil Pesanan (String dipisahkan koma)
            val specsString = selectedOptions.joinToString(", ")

            // --- PINDAH KE PAYMENT ACTIVITY ---
            val intent = Intent(this, PaymentActivity::class.java)

            // Passing data untuk dihitung
            intent.putExtra("EXTRA_SERVICE", binding.tvServiceTitle.text.toString())
            intent.putExtra("EXTRA_SPECS", specsString) // Kirim data spesifikasi
            intent.putExtra("EXTRA_NOTES", notes)
            intent.putExtra("EXTRA_DEADLINE", deadline)

            startActivity(intent)
            // Di sini nanti lanjut ke Firebase
        }
    }

    private fun showDatePicker() {
        // Ambil tahun, bulan, hari saat ini untuk default calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Saat user klik OK
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                // Format tanggal jadi string cantik (Contoh: 12 Desember 2023)
                val format =
                    SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) // Locale Indonesia
                binding.etDeadline.setText(format.format(calendar.time))
            },
            year, month, day
        )

        // PENTING: Disable tanggal masa lalu
        // System.currentTimeMillis() - 1000 agar hari ini masih bisa dipilih
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }
}