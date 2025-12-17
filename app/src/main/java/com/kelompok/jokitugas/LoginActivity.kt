package com.kelompok.jokitugas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // 1. Deklarasi variable binding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Inflate layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Setup UI Listeners (Interaksi awal)
        setupActionListeners()
    }

    private fun setupActionListeners() {
        // Contoh akses view tanpa findViewById
        binding.btnLogin.setOnClickListener {
            val username = binding.tilUsername.editText?.text.toString()
            val password = binding.tilPassword.editText?.text.toString()

            // 1. Cek Email
            if (username.isEmpty()) {
                binding.tilUsername.error = "Email or Phone is required"
                return@setOnClickListener // STOP di sini kalau email kosong
            } else {
                binding.tilUsername.error = null // Hapus error jika sudah diisi
            }

            // 2. Cek Password
            if (password.isEmpty()) {
                binding.tilPassword.error = "Password is required"
                return@setOnClickListener // STOP di sini kalau password kosong
            } else {
                binding.tilPassword.error = null // Hapus error jika sudah diisi
            }

            // A. Simpan Sesi ke SharedPreferences
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("CURRENT_USERNAME", username)
            editor.putBoolean("IS_LOGGED_IN", true)
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Tutup Login agar user tidak bisa back ke login lagi
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}