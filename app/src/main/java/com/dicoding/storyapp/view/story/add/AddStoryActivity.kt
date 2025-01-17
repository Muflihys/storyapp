package com.dicoding.storyapp.view.story.add

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.storyapp.utils.ViewModelFactory
import com.dicoding.storyapp.utils.createCustomTempFile
import com.dicoding.storyapp.utils.reduceFileImage
import com.dicoding.storyapp.utils.rotateBitmap
import com.dicoding.storyapp.utils.uriToFile
import com.dicoding.storyapp.view.home.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var factory: ViewModelFactory
    private lateinit var currentPhotoPath: String
    private var getFile: File? = null
    private val addStoryViewModel: AddStoryViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        setupViewModel()
        setupPermission()
        setupAction()
    }

    private fun setupAction() {
        binding.apply {
            btnTakePicture.setOnClickListener { capturePicture() }
            btnOpenFile.setOnClickListener { startGallery() }
            btnUpload.setOnClickListener { uploadStory() }
        }
    }

    private fun setupPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this@AddStoryActivity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setupViewModel() {
        factory = ViewModelFactory.getInstance(this)
    }

    private fun setupView() {
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.title_add_story)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile

            val result = rotateBitmap(
                BitmapFactory.decodeFile(getFile?.path),
                true
            )
            Glide.with(this@AddStoryActivity)
                .load(result)
                .into(binding.ivAddStory)
        }
    }

    private fun capturePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.dicoding.storyapp",
                it
            )
            currentPhotoPath = it.absolutePath

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)

            getFile = myFile
            Glide.with(this@AddStoryActivity)
                .load(selectedImg)
                .into(binding.ivAddStory)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadStory() {
        showLoading()
        addStoryViewModel.getSession().observe(this@AddStoryActivity) { session ->
            if (getFile != null) {
                val reducedFile = reduceFileImage(getFile as File)
                val requestImageFile = reducedFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    reducedFile.name,
                    requestImageFile
                )
                addStoryViewModel.uploadStory(
                    session.token,
                    imageMultipart,
                    binding.edtDescStory.text.toString().toRequestBody("text/plain".toMediaType())
                )

                // Observe upload response
                addStoryViewModel.uploadResponse.observe(this@AddStoryActivity) { response ->
                    if (response.error == false && !binding.edtDescStory.text.isNullOrEmpty()) {
                        navigateToMain()  // Navigate only if upload is successful
                    } else {
                        showToast(getString(R.string.upload_failed))
                        hideLoading()
                    }
                }

                // Observe error response (network failure, etc.)
                addStoryViewModel.toastText.observe(this@AddStoryActivity) { event ->
                    event.getContentIfNotHandled()?.let { message ->
                        showToast(message) // Show error message if upload fails
                        hideLoading()
                    }
                }
            } else {
                Toast.makeText(this@AddStoryActivity, getString(R.string.input_image), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        addStoryViewModel.isLoading.observe(this@AddStoryActivity) {
            binding.pbAdd.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    private fun hideLoading() {
        binding.pbAdd.visibility = View.GONE
    }

    private fun showToast(string: String) {
        Toast.makeText(this@AddStoryActivity, string, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
