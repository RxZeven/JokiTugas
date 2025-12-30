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


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // 1. Deklarasi variable binding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        
        // --- KONFIGURASI GOOGLE SIGN IN ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("992242004996-lbf7hsru0lrj4n1u489nop691o0qk7ec.apps.googleusercontent.com") // Ganti nanti dengan Web Client ID dari Firebase Console
            .requestEmail()
            .build()
            
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        // 2. Inflate layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Setup UI Listeners (Interaksi awal)
        setupActionListeners()
    }


    private fun setupActionListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.tilUsername.editText?.text.toString()
            val password = binding.tilPassword.editText?.text.toString()

            // 1. Cek Email
            if (email.isEmpty()) {
                binding.tilUsername.error = "Email or Phone is required"
                return@setOnClickListener
            } else {
                binding.tilUsername.error = null
            }

            // 2. Cek Password
            if (password.isEmpty()) {
                binding.tilPassword.error = "Password is required"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            // 🔐 LOGIN KE FIREBASE
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    checkUserRoleAndRedirect(email)
                }
                .addOnFailureListener {
                    binding.tilPassword.error = "Email or password is incorrect"
                }
        }
        
        // --- LOGIN GOOGLE ---
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
        
        // --- LOGIN FACEBOOK (Simulasi Toast Dulu) ---
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Login Facebook belum dikonfigurasi", Toast.LENGTH_SHORT).show()
        }
        
        // --- LOGIN TWITTER ---
        binding.btnTwitter.setOnClickListener {
            val provider = com.google.firebase.auth.OAuthProvider.newBuilder("twitter.com")

            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    // Redirect Twitter (Asumsi User Biasa dulu, karena Admin biasanya Email/Pass)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Twitter Login Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
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
    
    // Hasil balikan dari halaman Login Google
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Login Google Sukses -> Lanjut Authenticate ke Firebase
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
                    // Sign in success
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun checkUserRoleAndRedirect(email: String) {
        // ✅ SIMPAN SESSION
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPref.edit()
            .putString("CURRENT_EMAIL", email)
            .putBoolean("IS_LOGGED_IN", true)
            .apply()

        // 👑 CEK APAKAH INI ADMIN
        // Ganti "admin@jokitugas.com" dengan email admin yang kamu inginkan
        if (email == "admin@jokitugas.com") {
             Toast.makeText(this, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
             startActivity(Intent(this, AdminActivity::class.java))
        } else {
             startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}