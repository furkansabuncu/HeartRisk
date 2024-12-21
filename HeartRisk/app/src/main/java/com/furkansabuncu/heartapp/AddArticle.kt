package com.furkansabuncu.heartapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddArticle : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_article)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        val titleEditText: EditText = findViewById(R.id.textBaslik)
        val contentEditText: EditText = findViewById(R.id.editTextText)
        val submitButton: Button = findViewById(R.id.imageButton)

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            if (title.isNotBlank() && content.isNotBlank()) {
                val post = hashMapOf(
                    "title" to title,
                    "content" to content,
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("Post")
                    .add(post)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Veri başarıyla eklendi.", Toast.LENGTH_SHORT).show()
                        // Clear input fields
                        titleEditText.text.clear()
                        contentEditText.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Veri eklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Tüm alanları doldurunuz.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
