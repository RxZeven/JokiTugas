package com.kelompok.jokitugas

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityPromoDetailBinding
import java.util.concurrent.TimeUnit

class PromoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPromoDetailBinding
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        startTimer()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Fitur Copy Code
        binding.btnCopy.setOnClickListener {
            val code = binding.tvVoucherCode.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Promo Code", code)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Kode $code berhasil disalin!", Toast.LENGTH_SHORT).show()
        }

        // Tombol Pakai -> Arahkan ke Halaman Pilih Layanan
        binding.btnUsePromo.setOnClickListener {
            // Kita bisa kirim kode promo ini ke JokiOrderActivity agar otomatis terisi (jika ada fiturnya)
            val intent = Intent(this, JokiOrderActivity::class.java)
            // intent.putExtra("EXTRA_PROMO_CODE", binding.tvVoucherCode.text.toString())
            startActivity(intent)
            finish()
        }
    }

    private fun startTimer() {
        // Hitung mundur 2 jam (dalam milidetik)
        val duration = TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(45)

        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Konversi sisa waktu ke Jam:Menit:Detik
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                // Tampilkan ke UI (Format 00)
                binding.tvTimerHour.text = String.format("%02d", hours)
                binding.tvTimerMinute.text = String.format("%02d", minutes)
                binding.tvTimerSecond.text = String.format("%02d", seconds)
            }

            override fun onFinish() {
                binding.tvTimerSecond.text = "00"
                Toast.makeText(applicationContext, "Promo Berakhir!", Toast.LENGTH_SHORT).show()
                binding.btnUsePromo.isEnabled = false
                binding.btnUsePromo.text = "Promo Habis"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hentikan timer saat keluar agar tidak memory leak
        if (::timer.isInitialized) timer.cancel()
    }
}