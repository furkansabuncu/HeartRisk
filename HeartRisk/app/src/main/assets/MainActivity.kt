package com.furkansabuncu.heartapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import org.json.JSONObject;

class MainActivity extends AppCompatActivity {

    private lateinit var interpreter: Interpreter;
    private var meanValues: FloatArray? = null;
    private var scaleValues: FloatArray? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // JSON'dan scaler parametrelerini yükle
        loadScalerParams();

        if (meanValues == null || scaleValues == null) {
            Toast.makeText(this, "Scaler parametreleri yüklenemedi!", Toast.LENGTH_LONG).show();
            return;
        }

        // Modeli yükle
        val model = loadModelFile();
        val options = Interpreter.Options();
        interpreter = Interpreter(model, options);

        // Sabit değerler (age, sex, cp, vb.)
        val inputData = floatArrayOf(
            80f,  // Age
            1f,   // Sex (1 = Male)
            2f,   // CP (Chest pain type = 2, yüksek şiddetli ağrı)
            180f, // Trestbps (Resting blood pressure = 180, yüksek kan basıncı)
            300f, // Chol (Serum cholesterol = 300, çok yüksek kolesterol)
            1f,   // FBS (Fasting blood sugar = 1, diyabetli)
            2f,   // Restecg (Resting electrocardiographic results = 2, kalp problemi)
            130f, // Thalach (Maximum heart rate achieved = 130, düşük kalp hızı)
            1f,   // Exang (Exercise induced angina = 1)
            2.5f, // Oldpeak (Depression induced by exercise relative to rest = 2.5)
            1f,   // Slope (Slope of the peak exercise ST segment = 1)
            0f,   // CA (Number of major vessels colored by fluoroscopy = 0)
            2f    // Thal (Thalassemia = 2)
        );

        // Giriş verisini ölçeklendir
        val scaledInputData = scaleInputData(inputData);

        // Giriş verisini ByteBuffer'a dönüştür
        val byteBuffer = preprocessInputData(scaledInputData);

        // Çıktı dizisini oluştur (1x1 boyutunda)
        val output = Array(1) { FloatArray(1) }; // Modelden 1 sayı dönecek

        // Tahmin yap
        interpreter.run(byteBuffer, output);

        // Sonucu logla
        Log.d("ModelTest", "Tahmin Sonucuuuuu: ${output[0][0]}");

        // Sonucu ekranda göster
        val result = if (output[0][0] > 0.5) "More chance of heart attack" else "Less chance of heart attack";

        // Sonucu ekranda göster
        Toast.makeText(this, "Tahmin Sonucu: $result", Toast.LENGTH_LONG).show();
    }

    // JSON'dan scaler parametrelerini yükleyen fonksiyon
    private fun loadScalerParams() {
        try {
            val inputStream: InputStream = assets.open("scaler_params.json");
            val size = inputStream.available();
            val buffer = ByteArray(size);
            inputStream.read(buffer);
            inputStream.close();

            val jsonString = String(buffer, Charsets.UTF_8);
            val jsonObject = JSONObject(jsonString);

            val meanArray = jsonObject.getJSONArray("mean");
            val scaleArray = jsonObject.getJSONArray("scale");

            meanValues = FloatArray(meanArray.length()) {
                meanArray.getDouble(it).toFloat()
            };

            scaleValues = FloatArray(scaleArray.length()) {
                scaleArray.getDouble(it).toFloat()
            };
        } catch (e: Exception) {
            e.printStackTrace();
            Log.e("ScalerParams", "Hata: Scaler parametreleri yüklenemedi.");
        }
    }

    // Giriş verisini ölçeklendiren fonksiyon
    private fun scaleInputData(inputData: FloatArray): FloatArray {
        val scaledData = FloatArray(inputData.size);
        for (i in inputData.indices) {
            scaledData[i] = (inputData[i] - (meanValues?.get(i) ?: 0f)) / (scaleValues?.get(i) ?: 1f);
        }
        return scaledData;
    }

    // Giriş verisini ByteBuffer'a dönüştüren fonksiyon
    private fun preprocessInputData(inputData: FloatArray): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputData.size);
        byteBuffer.order(ByteOrder.nativeOrder()); // Native byte sıralama düzenine göre
        for (i in inputData.indices) {
            byteBuffer.putFloat(inputData[i]);
        }
        return byteBuffer;
    }

    // Model dosyasını yükleyen fonksiyon
    private fun loadModelFile(): ByteBuffer {
        val assetManager = assets;
        val fileDescriptor = assetManager.openFd("model.tflite");
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor);
        val fileChannel = inputStream.channel;
        val startOffset = fileDescriptor.startOffset;
        val declaredLength = fileDescriptor.declaredLength;
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Activity kapandığında modelin kapanması
    override fun onDestroy() {
        super.onDestroy();
        interpreter.close();
    }
}