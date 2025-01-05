package com.furkansabuncu.heartapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.furkansabuncu.heartapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()  // FirebaseAuth örneği oluşturuluyor
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Binding ile layout'a erişim sağla
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Authentication kullanarak güncel kullanıcı e-posta adresini al
        val userEmailFromDb = auth.currentUser?.email // Güncel kullanıcı e-posta adresi
        if (userEmailFromDb != null) {
            getUserProfile(userEmailFromDb)
        }

        // Çıkış yap butonunun işlevi
        binding.logoutButton.setOnClickListener {
            logoutUser()
        }

        // FloatingActionButton'ın tıklama işlemi
        binding.mainButton.setOnClickListener {
            // Ana eylem butonuna tıklanabilir aksiyon ekleyebilirsin (örneğin, kalp sağlığına dair öneriler vb.)
        }
    }

    private fun getUserProfile(email: String) {
        db.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val name = document.getString("name")
                    val surname = document.getString("surname")
                    val username = document.getString("username")

                    // Profil bilgilerini ekrana yansıt
                    binding.userName.text = "$name $surname"
                    binding.userEmail.text = email
                    // Profil fotoğrafını değiştirebilirsin
                    binding.profileImage.setImageResource(R.drawable.profilee) // Profil fotoğrafı
                } else {
                    // Verinin bulunmadığı durumda
                    println("No matching documents found.")
                }
            }
            .addOnFailureListener { exception ->
                // Hata durumunda yapılacak işlemler
                println("Error getting documents: $exception")
            }
    }


    private fun logoutUser() {
        // FirebaseAuth kullanarak çıkış işlemi
        auth.signOut()

        // Kullanıcıyı login ekranına yönlendir
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()  // Profil ekranını kapat
    }
}
