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
        binding.footerLayout.section1.setOnClickListener{
            val intent = Intent(this@MainActivity,MainPage::class.java)
            startActivity(intent)
            finish()
        }



        binding.cpInfoIcon.setOnClickListener {
            showExplanationDialog("Tipik Angina (0):\n" +
                    "\n" +
                    "Göğüste sıkışma, ağırlık hissi.\n" +
                    "Ağrı kola, çeneye veya sırta yayılabilir.\n" +
                    "Fiziksel aktivite veya stresle tetiklenir.\n" +
                    "Atipik Angina (1):\n" +
                    "\n" +
                    "Belirsiz göğüs rahatsızlığı.\n" +
                    "Yanma veya hazımsızlık hissi.\n" +
                    "Genellikle net bir tetikleyici yoktur.\n" +
                    "Non-anginal Ağrı (2):\n" +
                    "\n" +
                    "Keskin veya batıcı göğüs ağrısı.\n" +
                    "Sürekli bir ağrı olabilir.\n" +
                    "Dinlenme ya da fiziksel aktiviteyle ilişkili olmayabilir.\n" +
                    "Semptomsuz (3):\n" +
                    "\n" +
                    "Hiçbir göğüs ağrısı veya rahatsızlık hissi yoktur.\n" +
                    "Sessiz iskemi olarak da bilinir, genellikle başka testlerle tespit edilir.")
        }

        binding.trtbpsInfoIcon.setOnClickListener {
            showExplanationDialog("Dinlenme kan basıncı (mmHg cinsinden)." +
                    "Dinlenme sırasında ölçülen kan basıncı, kalbinizin ve damarlarınızın genel sağlığı hakkında bilgi verir." +
                    "Gerçekçi girişler genelde 70 - 180 mmHg arasında olacaktır.\n" +
                    "\n")
        }

        binding.cholInfoIcon.setOnClickListener {
            showExplanationDialog("Serum Kolesterol (mg/dL)\n" +
                    "Serum kolesterol, kanda toplam kolesterol seviyesini ifade eder ve kalp-damar hastalıkları riskini değerlendirmek için kullanılır.\n" +
                    "\n" +
                    "Kullanıcının Girebileceği Aralık\n" +
                    "Genel Aralık: 100 - 400 mg/dL\n" +
                    "(Gerçekçi değerler bu aralıkta yer alır.)")
        }

        binding.fbsInfoIcon.setOnClickListener {
            showExplanationDialog("Açlık Kan Şekeri (mg/dL) > 120 mg/dl), 1 = True, 0 = False\n" +
                    "Açlık kan şekeri, bireyin en az 8 saatlik açlık sonrası ölçülen kan şekeri seviyesidir. Diyabet ve insülin direnci gibi durumların tanısında kullanılır.\n" +
                    "\n" +
                    "Kullanıcının Girebileceği Aralık\n" +
                    "Genel Aralık: 50 - 300 mg/dL\n" +
                    "(Bu aralık, hem normal hem de anormal değerleri kapsamaktadır.)" +
                    "> 120 mg/dl), 1 = True, 0 = False")
        }

        binding.restecgInfoIcon.setOnClickListener {
            showExplanationDialog("Dinlenme sırasında kaydedilen EKG, kalp ritmi ve elektriksel aktivitesini değerlendirmek için kullanılır. İşte kısa açıklamalar:\n" +
                    "\n" +
                    "0 = Normal\n" +
                    "\n" +
                    "Tanım: Kalp ritmi ve elektriksel aktivite normaldir.\n" +
                    "Belirti: Hiçbir anormal semptom yoktur.\n" +
                    "1 = Anormal\n" +
                    "\n" +
                    "Tanım: Kalp ritmi veya elektriksel iletimde bozukluklar vardır.\n" +
                    "Belirti: Düzensiz kalp atışı, göğüs ağrısı, nefes darlığı.\n" +
                    "2 = Hipertrofi\n" +
                    "\n" +
                    "Tanım: Kalp kaslarının özellikle sol ventrikülün kalınlaşması.\n" +
                    "Belirti: Çabuk yorulma, çarpıntı, göğüs ağrısı.")
        }

        binding.thalachhInfoIcon.setOnClickListener {
            showExplanationDialog("Maksimum kalp hızı, egzersiz sırasında kalbin ulaşabileceği en yüksek hızdır. Genellikle, yaşa bağlı olarak hesaplanır.\n" +
                    "\n" +
                    "Hesaplama Yöntemi\n" +
                    "En yaygın hesaplama, 220 - yaş formülüne dayanır. Örneğin, 30 yaşındaki bir kişi için maksimum kalp hızı:\n" +
                    "\n" +
                    "220 - 30 = 190 bpm (dakika başına atım).")
        }

        binding.exngInfoIcon.setOnClickListener {
            showExplanationDialog("Egzersiz sırasında veya sonrasında ortaya çıkan göğüs ağrısı veya sıkışma hissi, kalp sağlığıyla ilgili önemli bir belirti olabilir. Angina, kalp kasına yeterli oksijen gitmemesi nedeniyle meydana gelir.\n" +
                    "\n" +
                    "1 = Var (Egzersiz Sonrası Angina Var)\n" +
                    "\n" +
                    "Tanım: Egzersiz sonrası göğüste ağrı, sıkışma veya baskı hissi oluşur. Bu durum genellikle kalp damarlarının daralması veya tıkanmasıyla ilişkilidir.\n" +
                    "Belirtiler: Göğüste ağrı, nefes darlığı, yorgunluk, çarpıntı.\n" +
                    "Önem: Kalp hastalıklarının erken belirtisi olabilir, tıbbi değerlendirme gerektirir.\n" +
                    "0 = Yok (Egzersiz Sonrası Angina Yok)\n" +
                    "\n" +
                    "Tanım: Egzersiz sonrası göğüs ağrısı veya rahatsızlık hissi yoktur.\n" +
                    "Belirtiler: Göğüste ağrı veya rahatsızlık yoktur, kişi egzersiz sonrası rahat hissedebilir.\n" +
                    "Önem: Normal bir durumdur, ancak uzun süreli izleme gerekebilir.")
        }

        binding.oldpeakInfoIcon.setOnClickListener {
            showExplanationDialog("gzersizle İlişkili ST Depresyonu için genellikle şu değerler girilebilir:\n" +
                    "\n" +
                    "0 = Yok (ST depresyonu yok)\n" +
                    "Tanım: Egzersiz sırasında ST segmentinde herhangi bir depresyon (aşağıya kayma) gözlemlenmez, yani kalp kası yeterli oksijeni alır.\n" +
                    "1 = Var (ST depresyonu var)\n" +
                    "Tanım: Egzersiz sırasında ST segmentinde depresyon gözlemlenir, bu da kalbin oksijen ihtiyacını karşılamakta zorlandığını ve potansiyel olarak kalp damarlarında daralma veya tıkanıklık olabileceğini gösterir.")
        }

        binding.slpInfoIcon.setOnClickListener {
            showExplanationDialog("Egzersiz sırasında eğim, kalbin elektriksel aktivitesindeki değişiklikleri değerlendiren bir parametredir ve genellikle EKG ile ölçülür. Bu eğim, kalbin çalışma durumu hakkında bilgi verir.\n" +
                    "\n" +
                    "0 = Düz\n" +
                    "Tanım: ST segmenti düz, yani yukarı veya aşağı doğru bir kayma yoktur. Kalp normal bir şekilde çalışıyor olabilir.\n" +
                    "1 = Yukarı Eğilimli\n" +
                    "Tanım: ST segmenti yukarı doğru kayar. Bu, kalbin oksijen ihtiyacını karşılamakta zorlanmadığını, ancak bazen kalp yükü arttığında gözlemlenebilir.\n" +
                    "2 = Aşağı Eğilimli\n" +
                    "Tanım: ST segmenti aşağı doğru kayar. Bu durum genellikle kalp damarlarında tıkanıklık veya daralma belirtisi olabilir, çünkü kalp egzersiz sırasında yeterli oksijeni alamayabilir.")
        }

        binding.caaInfoIcon.setOnClickListener {
            showExplanationDialog("Floroskopi, damarlar ve kalp yapıları hakkında bilgi veren bir görüntüleme yöntemidir. Bu yöntem, damarların durumunu ve olası tıkanıklıkları görmek için kullanılır. Özellikle kalp hastalıklarının değerlendirilmesinde faydalıdır.\n" +
                    "\n" +
                    "Değerler\n" +
                    "0 = Yok: Majör damarlarında herhangi bir tıkanıklık veya daralma tespit edilmemiştir.\n" +
                    "1 = Bir Damar: Bir majör damarda tıkanıklık veya daralma tespit edilmiştir.\n" +
                    "2 = İki Damar: İki majör damarda tıkanıklık veya daralma tespit edilmiştir.\n" +
                    "3 = Üç Damar: Üç majör damarda tıkanıklık veya daralma tespit edilmiştir.")
        }

        binding.thallInfoIcon.setOnClickListener {
            showExplanationDialog("Thal değerleri, kalp kasının oksijen alımı ve kan akışı ile ilgili anormallikleri değerlendiren bir parametredir. Thalium, genellikle kalp stresi testi sırasında kullanılır ve bu testte kalp kasının geçici veya kalıcı oksijen eksikliği durumları belirlenebilir.\n" +
                    "\n" +
                    "0 = Normal\n" +
                    "\n" +
                    "Tanım: Kalp kası normal şekilde oksijen alıyor, herhangi bir anormallik yok.\n" +
                    "1 = Düzeltilemez Kusur\n" +
                    "\n" +
                    "Tanım: Kalp kasında kalıcı bir hasar veya oksijen eksikliği vardır. Damar tıkanıklığı veya kalp kası hasarı nedeniyle bu durum düzeltilemez.\n" +
                    "2 = Reversibl Kusur\n" +
                    "\n" +
                    "Tanım: Kalp kasında geçici bir oksijen eksikliği veya hasar vardır. Bu durum, tedavi veya diğer iyileşme yöntemleriyle düzeltilebilir.")
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
