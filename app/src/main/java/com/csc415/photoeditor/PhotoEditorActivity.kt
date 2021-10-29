package com.csc415.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.csc415.photoeditor.transform.ColorBalance
import com.csc415.photoeditor.transform.Exposure
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

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

			try {
				bitmap = BitmapFactory.decodeStream(FileInputStream(File(imageUri)))
				val exif = ExifInterface(imageUri)
				val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
				val matrix = Matrix()

				// Make sure that the image rotation is correct. Sometimes it likes to be off +/- 90 degrees.
				when (orientation)
				{
					6 -> matrix.postRotate(90F)
					3 -> matrix.postRotate(180F)
					8 -> matrix.postRotate(270F)
				}

				// Recreate the bitmap using the rotation matrix.
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
				findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
			} catch (e: FileNotFoundException) {
				Log.e(tag, "Image file not found. Falling back to MainActivity", e)
				startActivity(Intent(this, MainActivity::class.java))
				val toast = Toast.makeText(applicationContext, "File Not Found", Toast.LENGTH_LONG)
				toast.show()
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
			var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
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
			var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
			bitmap = bitmap.copy(bitmap.config, true)
			bitmap = ColorBalance.doTransformation(bitmap)
			findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
		}
	}
}