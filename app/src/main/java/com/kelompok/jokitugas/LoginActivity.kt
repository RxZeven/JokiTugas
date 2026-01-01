package com.kelompok.jokitugas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jokitugas.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore // Tambahkan Import Database

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // Tambahkan Variabel DB
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance() // Inisialisasi DB
        
        // --- KONFIGURASI GOOGLE SIGN IN ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("992242004996-lbf7hsru0lrj4n1u489nop691o0qk7ec.apps.googleusercontent.com") 
            .requestEmail()
            .build()
            
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionListeners()
    }

    private fun setupActionListeners() {
        binding.btnLogin.setOnClickListener {
            val inputLogin = binding.tilUsername.editText?.text.toString().trim() // Bisa Email atau Username
            val password = binding.tilPassword.editText?.text.toString()

            // 1. Validasi Input
            if (inputLogin.isEmpty()) {
                binding.tilUsername.error = "Email atau Username wajib diisi"
                return@setOnClickListener
            } else {
                binding.tilUsername.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password wajib diisi"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            // 2. Cek apakah ini Email atau Username?
            if (inputLogin.contains("@")) {
                // Ini Email -> Login Biasa
                performLogin(inputLogin, password)
            } else {
                // Ini Username -> Cari Emailnya dulu di Database
                loginByUsername(inputLogin, password)
            }
        }
        
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
        
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Login Facebook belum dikonfigurasi", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnTwitter.setOnClickListener {
            val provider = com.google.firebase.auth.OAuthProvider.newBuilder("twitter.com")
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Twitter Login Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
    
    // Fungsi Khusus Login via Username (Nama)
    private fun loginByUsername(username: String, password: String) {
        // Cari user yang punya nama persis seperti inputan
        db.collection("users")
            .whereEqualTo("name", username) 
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Ketemu! Ambil email aslinya
                    val email = documents.documents[0].getString("email")
                    if (email != null) {
                        performLogin(email, password)
                    } else {
                        binding.tilUsername.error = "Data akun rusak (tidak ada email)"
                    }
                } else {
                    binding.tilUsername.error = "Username tidak ditemukan"
                }
            }
            .addOnFailureListener {
                binding.tilUsername.error = "Gagal mengecek username: ${it.message}"
            }
    }

    // Fungsi Login Inti (Tetap pakai Email di belakang layar)
    private fun performLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUserRoleAndRedirect(email)
            }
            .addOnFailureListener {
                binding.tilPassword.error = "Username/Email atau Password salah"
            }
    }
    
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun checkUserRoleAndRedirect(email: String) {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPref.edit()
            .putString("CURRENT_EMAIL", email)
            .putBoolean("IS_LOGGED_IN", true)
            .apply()

        if (email == "admin@jokitugas.com") {
             Toast.makeText(this, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
             startActivity(Intent(this, AdminActivity::class.java))
        } else {
             startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}