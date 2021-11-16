package com.csc415.photoeditor

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.*

const val PHOTO_URI = "com.csc415.photoeditor.photo_uri"

class MainActivity : AppCompatActivity()
{
	private val tag = this::class.java.simpleName
	private lateinit var currentPhotoPath: String

	/**
	 * Callback for pick image button.
	 *
	 * @author Will St. Onge
	 */
	private val getContent = registerForActivityResult(GetContent())
	{
		Log.d(tag, it.toString())
		startActivity(Intent(this, PhotoEditorActivity::class.java).apply {
			putExtra(PHOTO_URI, it.toString())
		})
	}

	/**
	 * Callback for take picture button.
	 *
	 * @author Will St. Onge
	 */
	private val takePicture = registerForActivityResult(TakePicture())
	{
		Log.d(tag, currentPhotoPath)
		startActivity(Intent(this, PhotoEditorActivity::class.java).apply {
			putExtra(PHOTO_URI, currentPhotoPath)
		})
	}

	/**
	 * Handles the activity creation by calling setup methods.
	 *
	 * @author Will St. Onge
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setupPickImageButton()
		setupTakePictureButton()
	}

	/**
	 * Sets up the 'Pick Image' button.
	 *
	 * @author Will St. Onge
	 */
	private fun setupPickImageButton()
	{
		// Set the onClick behavior.
		findViewById<Button>(R.id.pick_image).setOnClickListener {
			getContent.launch("image/*")
		}
	}

	/**
	 * Sets up the 'Take Picture' button.
	 *
	 * @author Will St. Onge
	 */
	private fun setupTakePictureButton()
	{
		// Set the onClick behavior.
		findViewById<Button>(R.id.take_picture).setOnClickListener {
			Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
				// Ensure that there's a camera activity to handle the intent
				takePictureIntent.resolveActivity(packageManager)?.also {
					// Create the File where the photo should go
					val photoFile: File? = try
					{
						createImageFile()
					}
					catch (e: IOException)
					{
						Toast.makeText(
							applicationContext, "Unable to create file.", Toast.LENGTH_LONG
						).show()
						Log.w(tag, e)
						null // Sets photoFile to null
					}

					// Continue only if the File was successfully created
					photoFile?.also {
						val photoURI: Uri = FileProvider.getUriForFile(
							this, BuildConfig.APPLICATION_ID + ".fileprovider", it
						)

						takePicture.launch(photoURI)
					}
				}
			}
		}
	}

	/**
	 * Creates an image file in the pictures directory.
	 *
	 * @author Will St. Onge
	 * @throws IOException If the file cannot be created.
	 * @return Returns an instance of the file where the image will be saved.
	 */
	@Throws(IOException::class)
	private fun createImageFile(): File
	{
		// Create a file name such that it is unique.
		val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
		val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

		return File.createTempFile(
			"JPEG_${timeStamp}_", ".jpg", storageDir
		).apply {
			currentPhotoPath = absolutePath
		}
	}
}