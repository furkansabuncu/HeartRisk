package com.furkansabuncu.heartapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase Auth'u başlat
        auth = FirebaseAuth.getInstance()

        // View bileşenlerini bağla
        val emailEditText: EditText = findViewById(R.id.emailTextRegister)
        val passwordEditText: EditText = findViewById(R.id.passTextRegister)
        val passwordValidationEditText: EditText = findViewById(R.id.passTextRegisterValidation)
        val registerButton: Button = findViewById(R.id.kayitButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val passwordValidation = passwordValidationEditText.text.toString()

            if (email.isBlank() || password.isBlank() || passwordValidation.isBlank()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordValidation) {
                Toast.makeText(this, "Şifreler uyuşmuyor.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase ile kullanıcı oluştur
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, GetInformation::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Kayıt sırasında hata oluştu: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
