package com.furkansabuncu.heartapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.EditText
import com.furkansabuncu.heartapp.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Kullanıcı zaten giriş yapmışsa direkt yönlendir
        if (auth.currentUser != null) {
            startActivity(Intent(this, WhichModel::class.java))
            finish()
        }

        // Google ile giriş butonu ve kodu kaldırıldı
        // Google giriş yerine sadece e-posta ve şifre ile giriş yapacağız

        binding.kayitButton.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }
    }

    // Email ve şifre ile giriş
    fun giris(view: android.view.View) {
        val email = findViewById<EditText>(R.id.emailText).text.toString()
        val password = findViewById<EditText>(R.id.passText).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "E-posta ve şifreyi giriniz", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Giriş başarılı
                    val user = auth.currentUser
                    startActivity(Intent(this, WhichModel::class.java))
                    finish()
                } else {
                    // Giriş başarısız
                    Toast.makeText(this, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
