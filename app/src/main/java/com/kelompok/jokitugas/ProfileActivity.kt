package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityProfileBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var map: org.osmdroid.views.MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Konfigurasi OSM (Wajib ada sebelum layout di-inflate)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupMap()
    }

    private fun setupUI() {
        // --- LOGIC DATA USER ---
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        // Ambil username, default "Pengguna Tamu" jika null
        val savedUsername = sharedPref.getString("CURRENT_USERNAME", "Pengguna Tamu")

        binding.tvUsername.text = savedUsername
        binding.tvStatus.text = "Member VIP"

        // --- BUTTON LISTENERS ---
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        binding.btnLogout.setOnClickListener {
            // Hapus Sesi (Clear Data)
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

//            Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show()

            // Kembali ke Login & Hapus History Stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        // --- BOTTOM NAVIGATION ---
        binding.menuHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.menuActivity.setOnClickListener {
            startActivity(Intent(this, AktivitasActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.menuChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun setupMap() {
        map = binding.mapView

        // 1. Tampilan Peta Standar
        map.setTileSource(TileSourceFactory.MAPNIK)

        // 2. Aktifkan Zoom
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)

        // 3. Titik Koordinat (Contoh: Monas)
        val startPoint = GeoPoint(-7.835318, 110.380491)
        map.controller.setCenter(startPoint)

        // 4. Tambahkan Pin (Marker)
        val startMarker = Marker(map)
        startMarker.position = startPoint
        startMarker.setAnchor(0.5f, 1.0f)
        startMarker.title = "Kantor Pusat Joki App"
        startMarker.snippet = "Buka: 09.00 - 17.00"
        startMarker.showInfoWindow()

        // --- FITUR KLIK MARKER UNTUK BUKA GOOGLE MAPS ---
        startMarker.setOnMarkerClickListener { marker, mapView ->
            // Tampilkan Info Window (Gelembung tulisan) dulu
            marker.showInfoWindow()

            // Tampilkan Dialog Konfirmasi (UX yang baik)
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Buka Google Maps?")
                .setMessage("Aplikasi akan mengarahkan Anda ke Google Maps untuk navigasi rute.")
                .setPositiveButton("Buka Rute") { _, _ ->
                    openGoogleMapsRoute(-7.835318, 110.380491)
                }
                .setNegativeButton("Batal", null)
                .show()

            return@setOnMarkerClickListener true
        }

        map.overlays.add(startMarker)
    }

    // Fungsi Pembantu untuk Membuka Aplikasi Google Maps
    private fun openGoogleMapsRoute(lat: Double, lng: Double) {
        // Format URI untuk Google Navigation
        val gmmIntentUri = android.net.Uri.parse("google.navigation:q=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        // Set agar membuka aplikasi Google Maps
        mapIntent.setPackage("com.google.android.apps.maps")

        // Cek apakah user punya Google Maps di HP-nya
        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            // Kalau gak punya Google Maps, buka di Browser
            val browserIntent = Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"))
            startActivity(browserIntent)
        }
    }

    // --- SIKLUS HIDUP MAP (PENTING UNTUK PERFORMA) ---
    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}