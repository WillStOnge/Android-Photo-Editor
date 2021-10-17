package com.csc415.photoeditor

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.Date

class MainActivity : AppCompatActivity()
{
	private val tag = MainActivity::class.java.simpleName
	private lateinit var currentPhotoPath: String

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
	 * Handles the activity result when the image is returned.
	 *
	 * @author Will St. Onge
	 */
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		// Checks for an not ok result.
		if (resultCode != Activity.RESULT_OK)
			return

		when (requestCode)
		{
			RequestType.PICK_IMAGE_REQUEST.type ->
			{
				Log.d(tag, data?.data.toString())

				val intent = Intent(this, PhotoEditorActivity::class.java).apply {
					putExtra(IntentExtraMessage.PHOTO_URI.extraName, data?.data.toString())
				}
				startActivity(intent)
			}

			RequestType.TAKE_PICTURE_REQUEST.type ->
			{
				Log.d(tag, currentPhotoPath)

				val intent = Intent(this, PhotoEditorActivity::class.java).apply {
					putExtra(IntentExtraMessage.PHOTO_URI.extraName, currentPhotoPath)
				}
				startActivity(intent)
			}

			else -> super.onActivityResult(requestCode, resultCode, data)
		}
	}

	/**
	 * Sets up the 'Pick Image' button.
	 *
	 * @author Will St. Onge
	 */
	private fun setupPickImageButton()
	{
		// Setup view elements.
		val pickImageButton = findViewById<Button>(R.id.pick_image)

		// Set the onClick behavior.
		pickImageButton.setOnClickListener {
			val intent = Intent(Intent.ACTION_GET_CONTENT)
			intent.type = "image/*"
			startActivityForResult(
				Intent.createChooser(intent, "Select Picture"), RequestType.PICK_IMAGE_REQUEST.type
			)
		}
	}

	/**
	 * Sets up the 'Take Picture' button.
	 *
	 * @author Will St. Onge
	 */
	private fun setupTakePictureButton()
	{
		// Setup view elements.
		val takePictureButton = findViewById<Button>(R.id.take_picture)

		// Set the onClick behavior.
		takePictureButton.setOnClickListener {
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
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
						startActivityForResult(
							takePictureIntent, RequestType.TAKE_PICTURE_REQUEST.type
						)
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