package com.furkansabuncu.heartapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.furkansabuncu.heartapp.databinding.ActivityMainPageBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainPage : AppCompatActivity() {

    lateinit var binding : ActivityMainPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityMainPageBinding.inflate(layoutInflater)
        val view : View= binding.root
        setContentView(view)



        // Kenar boşluklarını yönetmek için sistem çubuğu dinleyicisi
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase'den verileri çek ve arayüze bağla
        fetchAndBindData()

        binding.footerLayout.section1Line.setVisibility(View.VISIBLE);

        binding.mainButton.setOnClickListener{
            val intent = Intent(this@MainPage,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchAndBindData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Post")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(6)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val posts = mutableListOf<Post>()

                println("Veri: ${querySnapshot.documents}")


                // Verileri listeye ekleme
                for (document in querySnapshot) {
                    val title = document.getString("title") ?: "Bilinmiyor"
                    val content = document.getString("content") ?: "Bilinmiyor"
                    val timestamp = document.getLong("timestamp") ?: 0
                    posts.add(Post(title, content, timestamp))
                }

                // Verileri UI'a bağlama
                bindDataToViews(posts)
            }
            .addOnFailureListener { e ->
                println("Veri çekilirken hata oluştu: ${e.message}")
            }
    }

    private fun bindDataToViews(posts: List<Post>) {
        if (posts.isNotEmpty()) {
            // İlk CardView
            findViewById<TextView>(R.id.yeradi1aa)?.text = posts[0].title
            findViewById<TextView>(R.id.sehir1ae)?.text = posts[0].content
        }

        if (posts.size > 1) {
            // İkinci CardView
            findViewById<TextView>(R.id.yeradi1)?.text = posts[1].title
            findViewById<TextView>(R.id.sehir1)?.text = posts[1].content
        }
    }
}
