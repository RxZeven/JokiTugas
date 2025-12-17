package com.kelompok.jokitugas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Tombol Back
        binding.btnBack.setOnClickListener {
            finish() // Kembali ke Login
        }

        // Tombol Submit
        binding.btnSubmit.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString()

            if (email.isEmpty()) {
                binding.tilEmail.error = "Email is required"
            } else {
                binding.tilEmail.error = null

                // --- MOCKING / SIMULASI ---
                // Nanti baris ini diganti dengan: FirebaseAuth.sendPasswordResetEmail(email)
                Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_LONG).show()

                // Opsional: Langsung tutup halaman ini agar user balik ke login untuk cek email
                // finish()
            }
        }
    }
}