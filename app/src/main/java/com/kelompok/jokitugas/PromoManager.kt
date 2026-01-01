package com.kelompok.jokitugas

object PromoManager {
    
    data class Promo(
        val code: String, 
        val description: String,
        val discountValue: Int, 
        val isPercentage: Boolean, 
        val minTransaction: Int
    )

    // Daftar Promo yang tersedia (Bisa ditambah nanti)
    val availablePromos = listOf(
        Promo("MAHASISWA", "Diskon Spesial Mahasiswa 20rb", 20000, false, 50000),
        Promo("JOKIKILAT", "Diskon 10% untuk pesanan kilat", 10, true, 200000),
        Promo("AWALBULAN", "Potongan 50rb minimal order 300rb", 50000, false, 300000),
        Promo("SEMANGAT", "Potongan ongkir/admin 5rb", 5000, false, 0)
    )

    fun checkPromo(code: String, originalPrice: Int): Int {
        val promo = availablePromos.find { it.code.equals(code, ignoreCase = true) } ?: return 0
        
        // Cek Minimal Transaksi
        if (originalPrice < promo.minTransaction) {
            return 0 
        }

        // Hitung Diskon
        return if (promo.isPercentage) {
            (originalPrice * promo.discountValue / 100)
        } else {
            promo.discountValue
        }
    }
    
    fun getPromoDetails(code: String): Promo? {
        return availablePromos.find { it.code.equals(code, ignoreCase = true) }
    }
}