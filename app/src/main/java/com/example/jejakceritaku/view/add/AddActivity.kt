package com.example.jejakceritaku.view.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.jejakceritaku.R
import com.example.jejakceritaku.databinding.ActivityAddBinding
import com.example.jejakceritaku.data.pref.Result
import com.example.jejakceritaku.di.getImageUri
import com.example.jejakceritaku.di.reduceFileImage
import com.example.jejakceritaku.di.uriToFile
import com.example.jejakceritaku.view.ViewModelFactory
import com.example.jejakceritaku.view.main.MainActivity
import java.io.File

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private val viewModel: AddViewModel by viewModels { ViewModelFactory.getInstance(this) }


    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsAllow()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }


        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }

        isLoading(false)

    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showimage()
        }
    }

    private fun allPermissionsAllow() =
        ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isAccepted: Boolean ->
        val messageResId = if (isAccepted) R.string.allowed else R.string.rejected
        val message = getString(messageResId)
        showToast(message, Toast.LENGTH_LONG)
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }


    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showimage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showimage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }
    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageData = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageData.path}")
            val description = binding.descEditText.text.toString()

            viewModel.getSession().observe(this, { session ->
                val token = session.token
                viewModel.uploadImage(token, imageData, description).observe(this, { result ->
                    result?.let {
                        when (it) {
                            is Result.Loading -> {
                                isLoading(true)
                            }
                            is Result.Success -> {
                                showToast(it.data.message, Toast.LENGTH_LONG)
                                isLoading(false)
                                startActivity(Intent(this@AddActivity, MainActivity::class.java))
                            }
                            is Result.Error -> {
                                showToast(it.error, Toast.LENGTH_LONG)
                                isLoading(false)
                            }
                        }
                    }
                })
            })
        } ?: showToast(getString(R.string.image_empty), Toast.LENGTH_LONG)
    }


    private fun isLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String, length: Int) {
        Toast.makeText(this, message, length).show()
    }
}