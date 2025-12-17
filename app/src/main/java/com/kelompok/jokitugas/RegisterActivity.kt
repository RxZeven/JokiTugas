package com.kelompok.jokitugas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Tombol Back (Panah)
        binding.btnBack.setOnClickListener {
            finish() // Menutup activity ini, kembali ke Login
        }

        // Link "Sign In" di bawah
        binding.tvLogin.setOnClickListener {
            finish() // Sama, kembali ke Login
        }

        // Tombol Register
        binding.btnRegister.setOnClickListener {
            val name = binding.tilName.editText?.text.toString()
            val email = binding.tilEmail.editText?.text.toString()
            val password = binding.tilPassword.editText?.text.toString()
            val confirmPassword = binding.tilConfirmPassword.editText?.text.toString()

            // Validasi Dasar UI
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi Password Match
            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            } else {
                binding.tilConfirmPassword.error = null // Hapus error
            }

            // Jika lolos validasi
            Toast.makeText(this, "Registration logic here for $name", Toast.LENGTH_SHORT).show()
            // Di sini nanti tempat kita taruh kode Firebase Register
        }
    }
}