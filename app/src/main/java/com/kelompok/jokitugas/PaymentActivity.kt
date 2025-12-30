package com.kelompok.jokitugas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.UUID

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 1. Ambil Data dari Intent
        val serviceName = intent.getStringExtra("EXTRA_SERVICE") ?: "-"
        val specsString = intent.getStringExtra("EXTRA_SPECS") ?: "-" // "Python, Web"
        val notesString = intent.getStringExtra("EXTRA_NOTES") ?: "-"
        val deadlineString = intent.getStringExtra("EXTRA_DEADLINE") ?: "" // "15 Desember 2025"

        // 2. Tampilkan Data Mentah
        binding.tvServiceName.text = serviceName

        // Format tampilan spesifikasi agar berbaris ke bawah (ganti koma dengan enter)
        val formattedSpecs = specsString.replace(", ", "\n• ")
        binding.tvSpecs.text = "• $formattedSpecs"

        binding.tvNotes.text = if (notesString.isBlank()) "-" else notesString

        // 3. LOGIKA MATEMATIKA HARGA
        calculatePrice(specsString, deadlineString)

        setupListeners()
    }

    private fun calculatePrice(specsString: String, deadlineString: String) {
        val pricePerDayPerSpec = 150000

        // A. Hitung Jumlah Spesifikasi
        // Kita pecah string berdasarkan koma, lalu hitung jumlahnya
        // Contoh: "Python, Web" -> size = 2
        val specsCount = if (specsString.isBlank()) 0 else specsString.split(",").size

        // B. Hitung Jumlah Hari (Deadline - Hari Ini)
        val daysCount = calculateDaysDiff(deadlineString)

        // C. Rumus Total
        val totalPrice = specsCount * daysCount * pricePerDayPerSpec

        // Tampilkan ke UI
        binding.tvTotalPrice.text = formatRupiah(totalPrice)
        binding.tvDateInfo.text = "Durasi: $daysCount Hari kerja\n(Deadline: $deadlineString)"
    }

    private fun calculateDaysDiff(deadlineString: String): Int {
        if (deadlineString.isEmpty()) return 1 // Minimal 1 hari

        try {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val deadlineDate = format.parse(deadlineString) ?: Date()

            // Set jam hari ini ke 00:00:00 agar hitungannya akurat per hari
            val todayCalendar = Calendar.getInstance()
            todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            todayCalendar.set(Calendar.MINUTE, 0)
            todayCalendar.set(Calendar.SECOND, 0)
            todayCalendar.set(Calendar.MILLISECOND, 0)
            val todayDate = todayCalendar.time

            // Hitung selisih waktu dalam milidetik
            val diffInMillis = deadlineDate.time - todayDate.time

            // Ubah milidetik ke Hari
            val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

            // Jika hari H (0 hari) atau minus, anggap minimal 1 hari kerja
            return if (days < 1) 1 else days

        } catch (e: Exception) {
            e.printStackTrace()
            return 1 // Default error
        }
    }

    // Fungsi bikin format Rp yang cantik (Rp 150.000)
    private fun formatRupiah(number: Int): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        // Hilangkan desimal ,00 di belakang biar rapi
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(number)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // === LOGIKA ANIMASI PEMBAYARAN ===
        binding.btnConfirmPay.setOnClickListener {
            // 1. Disable tombol agar tidak diklik double
            binding.btnConfirmPay.isEnabled = false
            binding.btnConfirmPay.text = "Memproses..."

            // Mulai Rantai Animasi
            startSuccessAnimation()

            // Tombol di dalam Dialog Sukses
            binding.btnFinishOrder.setOnClickListener {
                // 1. AMBIL DATA DARI LAYAR
                val service = binding.tvServiceName.text.toString()
                val deadlineInfo = binding.tvDateInfo.text.toString()
                val price = binding.tvTotalPrice.text.toString() // Ambil "Rp 450.000"
                val notes = binding.tvNotes.text.toString()
                val specs = binding.tvSpecs.text.toString() // Ambil "Fitur Lengkap..."
                
                val orderId = UUID.randomUUID().toString()
                val paymentId = "PAY-${UUID.randomUUID()}" // ID Khusus Pembayaran
                val userId = auth.currentUser?.uid ?: "guest"
                val currentTime = System.currentTimeMillis()

                // 2. SIMPAN KE DATABASE SEMENTARA (Agar muncul di list lokal)
                val newOrder = OrderModel(
                    id = orderId,
                    serviceName = service,
                    deadline = deadlineInfo,
                    price = price,
                    status = "Diproses"
                )
                DummyData.orderHistory.add(newOrder)

                // 3. DATA PESANAN (ORDER)
                val orderMap = hashMapOf(
                    "id" to orderId,
                    "serviceTitle" to service, 
                    "deadline" to deadlineInfo,
                    "price" to price,
                    "status" to "Diproses",
                    "notes" to notes,
                    "specs" to specs, 
                    "paymentMethod" to "QRIS / Transfer",
                    "userId" to userId,
                    "createdAt" to currentTime
                )

                // 4. DATA PEMBAYARAN / STRUK (PAYMENT) - DIPISAH SESUAI PERMINTAAN
                val paymentMap = hashMapOf(
                    "paymentId" to paymentId,
                    "orderId" to orderId, // Relasi ke pesanan
                    "userId" to userId,
                    "amount" to price,
                    "paymentMethod" to "QRIS / Transfer",
                    "transactionDate" to currentTime,
                    "status" to "LUNAS", // Status pembayaran
                    "details" to "$service ($specs)" // Ringkasan apa yang dibayar
                )
                
                binding.btnFinishOrder.isEnabled = false
                binding.btnFinishOrder.text = "Menyimpan..."

                // Batch Write: Simpan ke dua koleksi sekaligus
                val batch = db.batch()
                
                val orderRef = db.collection("orders").document(orderId)
                batch.set(orderRef, orderMap)

                val paymentRef = db.collection("payments").document(paymentId)
                batch.set(paymentRef, paymentMap)

                batch.commit()
                    .addOnSuccessListener {
                        // 5. PINDAH KE MAIN ACTIVITY
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.btnFinishOrder.isEnabled = true
                        binding.btnFinishOrder.text = "Selesai"
                        Toast.makeText(this, "Gagal menyimpan ke Firebase: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun startSuccessAnimation() {
        val ball = binding.animBall
        val dialog = binding.successDialogContainer
        val fadeLayer = binding.fadeLayer

        // --- TAHAP PERSIAPAN ---
        // Hitung posisi awal (di luar layar bawah)
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        ball.translationY = screenHeight / 2 + 200f // Pindahkan jauh ke bawah
        ball.scaleX = 1f
        ball.scaleY = 1f
        ball.visibility = View.VISIBLE

        // Tampilkan layer gelap
        fadeLayer.visibility = View.VISIBLE
        fadeLayer.alpha = 0f
        fadeLayer.animate().alpha(1f).setDuration(300).start()


        // --- TAHAP 1: BOLA NAIK KE TENGAH ---
        ball.animate()
            .translationY(0f) // Kembali ke posisi tengah (sesuai constraint di XML)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(1.5f)) // Efek membal sedikit saat sampai tengah
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    // --- TAHAP 2: BOLA MEMBESAR (MELEDAK) ---
                    // Skala diperbesar sampai menutupi layar (misal 30x lipat)
                    ball.animate()
                        .scaleX(30f)
                        .scaleY(30f)
                        .setDuration(400)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                // --- TAHAP 3: TAMPILKAN DIALOG ---
                                ball.visibility = View.GONE // Sembunyikan bola raksasa
                                showSuccessDialog(dialog)
                            }
                        })
                        .start()
                }
            })
            .start()
    }

    private fun showSuccessDialog(dialogView: View) {
        dialogView.visibility = View.VISIBLE
        // Efek pop-up (muncul dari kecil ke ukuran normal)
        dialogView.scaleX = 0.7f
        dialogView.scaleY = 0.7f
        dialogView.alpha = 0f

        dialogView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()
    }
}
