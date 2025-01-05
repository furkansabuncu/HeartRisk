package com.furkansabuncu.heartapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.furkansabuncu.heartapp.databinding.ActivityDenemeBinding

class WhichModel : AppCompatActivity() {

    lateinit var  binding: ActivityDenemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDenemeBinding.inflate(layoutInflater)
        var view : View = binding.root
        setContentView(view)

        binding.selectModel1.setOnClickListener {
            val intent = Intent(this@WhichModel, MainActivity::class.java)
            startActivity(intent)
            finish()  // Eğer mevcut aktiviteyi bitirmek istiyorsanız, doğru şekilde kullanılmış.
        }





    }
}