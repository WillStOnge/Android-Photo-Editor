package com.csc415.photoeditor

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.csc415.photoeditor.transform.ColorBalance
import com.csc415.photoeditor.transform.Exposure
import com.csc415.photoeditor.util.saveToInternalStorage
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

const val REQUEST_CODE = 100

class PhotoEditorActivity : AppCompatActivity()
{
	private val tag = this::class.java.simpleName
	private lateinit var imageUri: String
	private lateinit var bitmap: Bitmap

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo_editor)

		// Get intent extra for the photo uri.
		if (intent.extras!!.containsKey(PHOTO_URI))
		{
			// Photo URI was sent.
			imageUri = intent.getStringExtra(PHOTO_URI)!!
			Log.d(tag, imageUri)

			try
			{
				// If the Uri is from the content scheme, open with the content resolver, otherwise, just use a FileInputStream.
				val stream: InputStream = if (imageUri.contains("content:")) contentResolver.openInputStream(
					Uri.parse(imageUri)
				)!!
				else FileInputStream(File(imageUri))

				bitmap = BitmapFactory.decodeStream(stream)
				Log.d(tag, bitmap.toString())
				val matrix = Matrix()

				matrix.postRotate(90F)

				// Recreate the bitmap using the rotation matrix.
				bitmap = Bitmap.createBitmap(
					bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
				)
				findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
			}
			catch (e: FileNotFoundException)
			{
				Log.e(tag, "Image file not found. Falling back to MainActivity", e)
				startActivity(Intent(this, MainActivity::class.java))
				Toast.makeText(applicationContext, "File Not Found", Toast.LENGTH_LONG).show()
			}

		}
		else // Invalid intent.
		{
			Log.w(tag, "No IMAGE_URI was sent with Intent. Falling back to MainActivity")
			startActivity(Intent(this, MainActivity::class.java))
		}

		setupExitButton()
		setupShareButton()
		setupExposureButton()
		setupColorBalance()
		setupSaveButton()
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
	{
		if (requestCode == REQUEST_CODE)
		{
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) saveToInternalStorage(
				bitmap, this
			)
			else Toast.makeText(this, "Please provide the required permissions", Toast.LENGTH_SHORT)
				.show()
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}

	/**
	 * Sets up the 'Save' button.
	 *
	 * @author Anthony Bosch
	 */
	private fun setupSaveButton()
	{
		// Setup view elements.
		val saveButton = findViewById<Button>(R.id.save)

		// Set onClick behavior.
		saveButton.setOnClickListener {
			saveToInternalStorage(bitmap, this)
		}
	}

	/**
	 * Sets up the 'Exit' button.
	 *
	 * @author Will St. Onge
	 */
	private fun setupExitButton()
	{
		// Setup view elements.
		val exitButton = findViewById<Button>(R.id.exit)

		// Set the onClick behavior.
		exitButton.setOnClickListener {
			startActivity(Intent(this, MainActivity::class.java))
		}
	}

	/**
	 * Sets up the 'Share' button.
	 *
	 * @author Will St. Onge
	 */
	private fun setupShareButton()
	{
		// Setup view elements.
		val shareButton = findViewById<Button>(R.id.share)

		// Set the onClick behavior.
		shareButton.setOnClickListener {
			val intent = Intent(Intent.ACTION_SEND)
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			intent.type = "image/*"
			intent.putExtra(Intent.EXTRA_STREAM, imageUri)

			startActivity(Intent.createChooser(intent, "Share to"))
		}
	}

	/**
	 * Sets up the 'Auto-Expose' button.
	 *
	 * @author Will St. Onge
	 */
	private fun setupExposureButton()
	{
		// Setup view elements.
		val exposeButton = findViewById<Button>(R.id.expose)

		exposeButton.setOnClickListener {
			bitmap = bitmap.copy(bitmap.config, true)
			bitmap = Exposure.doTransformation(bitmap)
			findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
		}
	}

	/**
	 * Sets up the 'Color-Balance' button
	 *
	 * @author Anthony Bosch
	 */
	private fun setupColorBalance()
	{
		// Setup view elements
		val colorBalanceButton = findViewById<Button>(R.id.balance)

		colorBalanceButton.setOnClickListener {
			bitmap = bitmap.copy(bitmap.config, true)
			bitmap = ColorBalance.doTransformation(bitmap)
			findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
		}
	}
}
