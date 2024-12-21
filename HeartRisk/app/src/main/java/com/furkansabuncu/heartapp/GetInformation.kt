package com.furkansabuncu.heartapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GetInformation : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_information)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val nameField: EditText = findViewById(R.id.nameText)
        val surnameField: EditText = findViewById(R.id.surnameText3)
        val usernameField: EditText = findViewById(R.id.usernameText)
        val saveButton: Button = findViewById(R.id.kayitButton2)

        saveButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val surname = surnameField.text.toString().trim()
            val username = usernameField.text.toString().trim()

            if (name.isEmpty() || surname.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid

            if (userId != null) {
                val userMap = hashMapOf(
                    "name" to name,
                    "surname" to surname,
                    "username" to username,
                    "email" to auth.currentUser?.email
                )

                firestore.collection("Users").document(userId)
                    .set(userMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                        // Ana sayfaya yönlendirme
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Bilgiler kaydedilemedi: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Kullanıcı oturumu bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
