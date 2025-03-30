package com.example.wastewatchers

import android.util.Log
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONArray
import java.io.File
import java.io.IOException

class RecycleActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_CODE = 1001
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recycle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonToHome = findViewById<Button>(R.id.button_to_home)
        val buttonTakePhoto = findViewById<Button>(R.id.button_take_photo)
        val buttonUploadPhoto = findViewById<Button>(R.id.button_upload_photo)

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
            != PackageManager.PERMISSION_GRANTED
        ) {
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
            Log.e("RecycleActivity", "Failed to convert URI to file: ${e.message}")
            throw e
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("RecycleActivity", "onActivityResult called. RequestCode: $requestCode, ResultCode: $resultCode")

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    Log.d("RecycleActivity", "Camera image URI: $imageUri")
                    Toast.makeText(this, "Photo Captured!", Toast.LENGTH_SHORT).show()

                    val imageFile = uriToFile(this, imageUri)
                    classifyImageWithHuggingFace(imageFile) { result ->
                        runOnUiThread {
                            Log.d("RecycleActivity", "Classification Result: $result")
                            Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    Log.d("RecycleActivity", "Gallery image selected: ${data?.data}")

                    if (data?.data == null) {
                        Log.e("RecycleActivity", "Gallery image URI is null!")
                        return
                    }

                    imageUri = data.data!!
                    Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show()

                    val imageFile = uriToFile(this, imageUri)
                    classifyImageWithHuggingFace(imageFile) { result ->
                        runOnUiThread {
                            Log.d("RecycleActivity", "Classification Result: $result")
                            Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else -> {
                    Log.w("RecycleActivity", "Unhandled request code: $requestCode")
                }
            }
        } else {
            Log.w("RecycleActivity", "Result not OK: $resultCode")
        }
    }

    fun classifyImageWithHuggingFace(imageFile: File, onResult: (String) -> Unit) {
        val client = OkHttpClient()

        val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/microsoft/resnet-50")
            .addHeader("Authorization", "Bearer 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")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onResult("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val predictions = JSONArray(body)
                    val top = predictions.getJSONObject(0)
                    val label = top.getString("label")
                    val score = top.getDouble("score")
                    onResult("Top label: $label (${(score * 100).toInt()}%)")
                } else {
                    onResult("Failed: ${body ?: "No response"}")
                }
            }
        })
    }
}
