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

    private var currentTotalPrice: Int = 0
    private var discountAmount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 1. Ambil Data dari Intent
        val serviceName = intent.getStringExtra("EXTRA_SERVICE") ?: "-"
        val specsString = intent.getStringExtra("EXTRA_SPECS") ?: "-" 
        val notesString = intent.getStringExtra("EXTRA_NOTES") ?: "-"
        val deadlineString = intent.getStringExtra("EXTRA_DEADLINE") ?: "" 

        // 2. Tampilkan Data Mentah
        binding.tvServiceName.text = serviceName

        val formattedSpecs = specsString.replace(", ", "\n• ")
        binding.tvSpecs.text = "• $formattedSpecs"

        binding.tvNotes.text = if (notesString.isBlank()) "-" else notesString

        // 3. LOGIKA MATEMATIKA HARGA
        calculatePrice(specsString, deadlineString)

        setupListeners()
    }

    private fun calculatePrice(specsString: String, deadlineString: String) {
        val pricePerDayPerSpec = 150000

        val specsCount = if (specsString.isBlank()) 0 else specsString.split(",").size
        val daysCount = calculateDaysDiff(deadlineString)

        // Rumus Total Awal
        currentTotalPrice = specsCount * daysCount * pricePerDayPerSpec

        updatePriceUI()
        binding.tvDateInfo.text = "Durasi: $daysCount Hari kerja\n(Deadline: $deadlineString)"
    }

    private fun updatePriceUI() {
        val finalPrice = if ((currentTotalPrice - discountAmount) < 0) 0 else (currentTotalPrice - discountAmount)
        binding.tvTotalPrice.text = formatRupiah(finalPrice)
        
        if (discountAmount > 0) {
            binding.tvOriginalPrice.visibility = View.VISIBLE
            binding.tvDiscountInfo.visibility = View.VISIBLE
            
            // Coret harga asli
            binding.tvOriginalPrice.text = formatRupiah(currentTotalPrice)
            binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            
            binding.tvDiscountInfo.text = "Hemat ${formatRupiah(discountAmount)}"
        } else {
             binding.tvOriginalPrice.visibility = View.GONE
             binding.tvDiscountInfo.visibility = View.GONE
        }
    }

    private fun calculateDaysDiff(deadlineString: String): Int {
        if (deadlineString.isEmpty()) return 1 

        try {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val deadlineDate = format.parse(deadlineString) ?: Date()

            val todayCalendar = Calendar.getInstance()
            todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            todayCalendar.set(Calendar.MINUTE, 0)
            todayCalendar.set(Calendar.SECOND, 0)
            todayCalendar.set(Calendar.MILLISECOND, 0)
            val todayDate = todayCalendar.time

            val diffInMillis = deadlineDate.time - todayDate.time
            val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

            return if (days < 1) 1 else days

        } catch (e: Exception) {
            e.printStackTrace()
            return 1 
        }
    }

    private fun formatRupiah(number: Int): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(number)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        // --- LOGIKA KODE PROMO ---
        binding.btnApplyPromo.setOnClickListener {
            val code = binding.etPromoCode.text.toString().trim()
            if (code.isNotEmpty()) {
                val discount = PromoManager.checkPromo(code, currentTotalPrice)
                if (discount > 0) {
                    discountAmount = discount
                    updatePriceUI()
                    
                    val detail = PromoManager.getPromoDetails(code)
                    Toast.makeText(this, "Promo Berhasil: ${detail?.description}", Toast.LENGTH_SHORT).show()
                    binding.etPromoCode.isEnabled = false // Kunci input
                    binding.btnApplyPromo.isEnabled = false
                    binding.btnApplyPromo.text = "Terpakai"
                } else {
                    Toast.makeText(this, "Kode promo tidak valid atau tidak memenuhi syarat", Toast.LENGTH_SHORT).show()
                    discountAmount = 0
                    updatePriceUI()
                }
            }
        }

        // === LOGIKA ANIMASI PEMBAYARAN ===
        binding.btnConfirmPay.setOnClickListener {
            binding.btnConfirmPay.isEnabled = false
            binding.btnConfirmPay.text = "Memproses..."

            startSuccessAnimation()

            binding.btnFinishOrder.setOnClickListener {
                val service = binding.tvServiceName.text.toString()
                val deadlineInfo = binding.tvDateInfo.text.toString()
                val finalPrice = binding.tvTotalPrice.text.toString() 
                val notes = binding.tvNotes.text.toString()
                val specs = binding.tvSpecs.text.toString() 
                
                val orderId = UUID.randomUUID().toString()
                val paymentId = "PAY-${UUID.randomUUID()}" 
                val userId = auth.currentUser?.uid ?: "guest"
                val currentTime = System.currentTimeMillis()

                val orderMap = hashMapOf(
                    "id" to orderId,
                    "serviceTitle" to service, 
                    "deadline" to deadlineInfo,
                    "price" to finalPrice,
                    "status" to "Diproses",
                    "notes" to notes,
                    "specs" to specs, 
                    "paymentMethod" to "QRIS / Transfer",
                    "userId" to userId,
                    "createdAt" to currentTime
                )

                val paymentMap = hashMapOf(
                    "paymentId" to paymentId,
                    "orderId" to orderId, 
                    "userId" to userId,
                    "amount" to finalPrice,
                    "paymentMethod" to "QRIS / Transfer",
                    "transactionDate" to currentTime,
                    "status" to "LUNAS", 
                    "details" to "$service ($specs)",
                    "promoUsed" to binding.etPromoCode.text.toString()
                )
                
                binding.btnFinishOrder.isEnabled = false
                binding.btnFinishOrder.text = "Menyimpan..."

                val batch = db.batch()
                
                val orderRef = db.collection("orders").document(orderId)
                batch.set(orderRef, orderMap)

                val paymentRef = db.collection("payments").document(paymentId)
                batch.set(paymentRef, paymentMap)

                batch.commit()
                    .addOnSuccessListener {
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

        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        ball.translationY = screenHeight / 2 + 200f 
        ball.scaleX = 1f
        ball.scaleY = 1f
        ball.visibility = View.VISIBLE

        fadeLayer.visibility = View.VISIBLE
        fadeLayer.alpha = 0f
        fadeLayer.animate().alpha(1f).setDuration(300).start()

        ball.animate()
            .translationY(0f) 
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(1.5f)) 
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    ball.animate()
                        .scaleX(30f)
                        .scaleY(30f)
                        .setDuration(400)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                ball.visibility = View.GONE 
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
