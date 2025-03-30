package com.example.wastewatchers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class UpcycleActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_CODE = 1001
    private lateinit var imageUri: Uri
    private var classifiedLabel: String = ""
    private lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upcycle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonToHome = findViewById<Button>(R.id.button_to_home)
        val buttonTakePhoto = findViewById<Button>(R.id.button_take_photo)
        val buttonUploadPhoto = findViewById<Button>(R.id.button_upload_photo)

        responseTextView = findViewById(R.id.text_ai_response)
        responseTextView.movementMethod = ScrollingMovementMethod()

        buttonToHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonTakePhoto.setOnClickListener {
            checkCameraPermission()
        }

        buttonUploadPhoto.setOnClickListener {
            openGallery()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = File.createTempFile("IMG_", ".jpg", cacheDir)
        imageUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
            tempFile
        } catch (e: Exception) {
            Log.e("UpcycleActivity", "Failed to convert URI to file: ${e.message}")
            throw e
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageFile = uriToFile(this, imageUri)
                    classifyImageWithHuggingFace(imageFile)
                }
                GALLERY_REQUEST_CODE -> {
                    if (data?.data != null) {
                        imageUri = data.data!!
                        val imageFile = uriToFile(this, imageUri)
                        classifyImageWithHuggingFace(imageFile)
                    } else {
                        Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun classifyImageWithHuggingFace(imageFile: File) {
        val client = OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS).build()
        val token = "INSERT-TOKEN-HERE"

        val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/microsoft/resnet-50")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@UpcycleActivity, "Hugging Face error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val predictions = JSONArray(body)
                    val top = predictions.getJSONObject(0)
                    classifiedLabel = top.getString("label")
                    runOnUiThread {
                        Toast.makeText(this@UpcycleActivity, "Classified: $classifiedLabel", Toast.LENGTH_SHORT).show()
                        generateUpcyclingTip()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UpcycleActivity, "Classification failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun generateUpcyclingTip() {
        val prompt = "I have $classifiedLabel. How can I upcycle this item? Keep it short."
        Log.d("UpcycleActivity", "Prompt: $prompt")
        runOnUiThread {
            responseTextView.text = "Thinking..."
        }

        sendToOpenAI(prompt) { answer ->
            runOnUiThread {
                Log.d("UpcycleActivity", "OpenAI response: $answer")
                responseTextView.text = answer
            }
        }
    }

    fun sendToOpenAI(prompt: String, onResult: (String) -> Unit) {
        val client = OkHttpClient()
        val apiKey = "INSERT-API-KEY-HERE"
        val requestBody = """
            {
                "model": "gpt-4o-mini",
                "max_tokens": 75,
                "temperature": 0.3,
                "messages": [
                    {"role": "user", "content": "$prompt"}
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult("OpenAI error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val answer = json
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    onResult(answer)
                } else {
                    onResult("OpenAI failed: ${body ?: "No response"}")
                }
            }
        })
    }
}
