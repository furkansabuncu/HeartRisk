package com.furkansabuncu.heartapp

import android.R
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.furkansabuncu.heartapp.databinding.ActivityMainBinding
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Modeli yükle
        val model = loadModelFile()
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)


        val genderOptions = listOf("Erkek", "Kadın")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sexSpinner.adapter = adapter


        binding.footerLayout.section4.setOnClickListener{
            val intent = Intent(this@MainActivity,Profile::class.java)
            startActivity(intent)
            binding.footerLayout.section1Line.visibility = View.GONE  // Bileşeni tamamen gizler

            finish()
        }



        binding.cpInfoIcon.setOnClickListener {
            showExplanationDialog("Göğüs ağrısı tipi: 0 = Tipik angina, 1 = Atipik angina, 2 = Non-anginal ağrı, 3 = Semptomsuz.")
        }

        binding.trtbpsInfoIcon.setOnClickListener {
            showExplanationDialog("Dinlenme kan basıncı (mmHg cinsinden).")
        }

        binding.cholInfoIcon.setOnClickListener {
            showExplanationDialog("Serum kolesterol değeri (mg/dl cinsinden).")
        }

        binding.fbsInfoIcon.setOnClickListener {
            showExplanationDialog("Açlık kan şekeri (>120 mg/dl): 1 = True, 0 = False.")
        }

        binding.restecgInfoIcon.setOnClickListener {
            showExplanationDialog("Dinlenme elektrokardiyografisi sonuçları: 0 = Normal, 1 = Anormal, 2 = Hipertrofi.")
        }

        binding.thalachhInfoIcon.setOnClickListener {
            showExplanationDialog("Maksimum kalp hızı.")
        }

        binding.exngInfoIcon.setOnClickListener {
            showExplanationDialog("Egzersiz sonrası angina: 1 = Var, 0 = Yok.")
        }

        binding.oldpeakInfoIcon.setOnClickListener {
            showExplanationDialog("Egzersizle ilişkili ST depresyonu.")
        }

        binding.slpInfoIcon.setOnClickListener {
            showExplanationDialog("Egzersiz sırasında eğim: 0 = Düz, 1 = Yukarı eğimli, 2 = Aşağı eğimli.")
        }

        binding.caaInfoIcon.setOnClickListener {
            showExplanationDialog("Floroskopi ile tespit edilen majör damar sayısı (0-3 arasında).")
        }

        binding.thallInfoIcon.setOnClickListener {
            showExplanationDialog("Thal değerleri: 0 = Normal, 1 = Düzeltilemez kusur, 2 = Reversibl kusur.")
        }




        // Butona tıklama olayını ekleyelim
        binding.submitButton.setOnClickListener {

            val selectedGender = binding.sexSpinner.selectedItem.toString()
            val genderValue = if (selectedGender == "Erkek") 0f else 1f

            // Giriş verilerini alalım
            val inputData = floatArrayOf(
                binding.ageEditText.text.toString().toFloat(),
                genderValue,
                binding.cpEditText.text.toString().toFloat(),
                binding.trtbpsEditText.text.toString().toFloat(),
                binding.cholEditText.text.toString().toFloat(),
                binding.fbsEditText.text.toString().toFloat(),
                binding.restecgEditText.text.toString().toFloat(),
                binding.thalachhEditText.text.toString().toFloat(),
                binding.exngEditText.text.toString().toFloat(),
                binding.oldpeakEditText.text.toString().toFloat(),
                binding.slpEditText.text.toString().toFloat(),
                binding.caaEditText.text.toString().toFloat(),
                binding.thallEditText.text.toString().toFloat()

            )

            // JSON dosyasından verileri ölçeklendir
            val scaledInputData = scaleInputData(inputData)

            // Giriş verisini ByteBuffer'a dönüştür
            val byteBuffer = preprocessInputData(scaledInputData)

            // Çıktı dizisini oluştur (1x1 boyutunda)
            val output = Array(1) { FloatArray(1) } // Modelden 1 sayı dönecek

            // Sonucu "Sonuç hesaplanıyor..." olarak göster
            binding.resultTextView.text = "Sonuç hesaplanıyor..."
            binding.resultTextView.setVisibility(View.VISIBLE)

            // Tahmin yap
            interpreter.run(byteBuffer, output)

            // Sonucu logla
            Log.d("ModelTest", "Tahmin Sonucu: ${output[0][0]}")

            // Sonucu ekranda göster
            val result = if (output[0][0] > 0.5) "Daha yüksek kalp krizi riski" else "Daha düşük kalp krizi riski"

            // Sonuç gösterildiği mesajı
            Toast.makeText(this, "Tahmin Sonucu: $result", Toast.LENGTH_LONG).show()

            // 3 saniye bekle ve ardından sonuca göre işlem yap
            Handler().postDelayed({
                // "Sonuç hesaplanıyor..." mesajını gizle
                binding.resultTextView.setVisibility(View.GONE)

                // Kötü sonuç ise kötü sonuç ekranına yönlendir
                if (output[0][0] > 0.5) {
                    val intent = Intent(this, BadResult::class.java)
                    startActivity(intent)
                }
                else if(output[0][0] < 0.5){
                    val intent = Intent(this, GoodResult::class.java)
                    startActivity(intent)
                }
            }, 3000) // 3 saniye bekleme
        }


    }

    // Model dosyasını yükleyen fonksiyon
    private fun loadModelFile(): ByteBuffer {
        val assetManager = assets
        val fileDescriptor = assetManager.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Dizi verisini ölçeklendiren fonksiyon
    private fun scaleInputData(inputData: FloatArray): FloatArray {
        val assetManager = assets
        try {
            val inputStream = assetManager.open("scaler_params.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val meanArray = jsonObject.getJSONArray("mean")
            val scaleArray = jsonObject.getJSONArray("scale")

            val scaledData = FloatArray(inputData.size)
            for (index in inputData.indices) {
                val value = inputData[index]
                val mean = meanArray.getDouble(index).toFloat()
                val scale = scaleArray.getDouble(index).toFloat()

                // Debug log: Mean, Scale ve Giriş değerlerini kontrol et
                Log.d("ScalerParams", "Index: $index, Input Value: $value, Mean: $mean, Scale: $scale")

                scaledData[index] = (value - mean) / scale
            }
            return scaledData
        } catch (e: Exception) {
            Log.e("ScalerParams", "Scaler JSON file loading error", e)
            return inputData // Hata durumunda orijinal veriyi döndür
        }
    }

    // Giriş verisini ByteBuffer'a dönüştüren fonksiyon
    private fun preprocessInputData(inputData: FloatArray): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputData.size)
        byteBuffer.order(ByteOrder.nativeOrder())  // Native byte sıralama düzenine göre
        for (i in inputData.indices) {
            byteBuffer.putFloat(inputData[i])
        }
        return byteBuffer
    }

    // Activity kapandığında modelin kapanması
    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }
    // Açıklama göstermek için bir fonksiyon ekle
    private fun showExplanationDialog(explanation: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Açıklama")
        builder.setMessage(explanation)
        builder.setPositiveButton("Tamam") { dialog, _ ->
            dialog.dismiss() // Kullanıcı "Tamam" dediğinde dialogu kapat
        }
        builder.create().show()
    }

}
