package com.csc415.photoeditor

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.csc415.photoeditor.model.BitmapViewModel
import com.csc415.photoeditor.transform.ColorBalance
import com.csc415.photoeditor.transform.ColorInvert
import com.csc415.photoeditor.transform.Exposure
import com.csc415.photoeditor.util.compressImage
import com.csc415.photoeditor.util.insertImage
import java.io.*

class PhotoEditorActivity : AppCompatActivity()
{
	private val tag = this::class.java.simpleName
	private lateinit var imageUri: String
	private lateinit var bitmapModel: BitmapViewModel

	/**
	 * Method will be called when the activity is created. It will load the bitmap from disk and setup the buttons at the bottom of the page.
	 *
	 * @author Will St. Onge
	 */
	@Suppress("DEPRECATION")
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo_editor)

		// Initialize view model for this instance of the activity.
		bitmapModel = ViewModelProviders.of(this).get(BitmapViewModel::class.java)

		// Get intent extra for the photo uri.
		if (intent.extras!!.containsKey(PHOTO_URI))
		{
			// Photo URI was sent.
			imageUri = intent.getStringExtra(PHOTO_URI)!!
			Log.d(tag, imageUri)

			try
			{
				if (bitmapModel.bitmap == null)
				{
					// If the Uri is from the content scheme, open with the content resolver, otherwise, just use a FileInputStream.
					val stream: InputStream = if (imageUri.contains("content:")) contentResolver.openInputStream(
						Uri.parse(imageUri)
					)!!
					else FileInputStream(File(imageUri))

					// Rotate the bitmap 90 degrees.
					val matrix = Matrix()
					matrix.postRotate(90F)

					// Scale and compress the bitmap.
					val display = windowManager.defaultDisplay
					var bitmap = compressImage(stream, display.width, display.height)

					// Recreate the bitmap using the rotation matrix.
					bitmap = Bitmap.createBitmap(
						bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
					)
					findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
					bitmapModel.originalImage = bitmap
					bitmapModel.bitmap = bitmap
				}
				else findViewById<ImageView>(R.id.photo).setImageBitmap(bitmapModel.bitmap)
			}
			catch (e: FileNotFoundException)
			{
				Log.e(
					tag,
					"Image file not found or couldn't be opened. Falling back to MainActivity",
					e
				)
				startActivity(Intent(this, MainActivity::class.java))
				Toast.makeText(applicationContext, "Could not open image.", Toast.LENGTH_LONG)
					.show()
			}
		}
		else // Invalid intent.
		{
			Log.w(tag, "No IMAGE_URI was sent with Intent. Falling back to MainActivity")
			startActivity(Intent(this, MainActivity::class.java))
		}

		setupExitButton()
		setupShareButton()
		setupSaveButton()
		setupUndoButton()
		setupExposureButton()
		setupColorBalance()
		setUpColorInvert()
	}

	private fun setupUndoButton() {
		val undo = findViewById<Button>(R.id.undo)
		undo.setOnClickListener {
			findViewById<ImageView>(R.id.photo).setImageBitmap(bitmapModel.originalImage)
			findViewById<Button>(R.id.invert).setTextColor(Color.YELLOW)
			findViewById<Button>(R.id.expose).setTextColor(Color.YELLOW)
			findViewById<Button>(R.id.balance).setTextColor(Color.YELLOW)
		}
	}

	/**
	 * Sets up Color inversion
	 *
	 * @author Trevor Sears
	 */

	private fun setUpColorInvert()
	{
		val colorInvert = findViewById<Button>(R.id.invert)
		colorInvert.setOnClickListener {
			colorInvert.setTextColor(Color.CYAN)
			var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
			bitmap = bitmap.copy(bitmap.config, true)
			bitmap = ColorInvert.doTransformation(bitmap)
			findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
			bitmap.also { bitmapModel.bitmap = it }
		}
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
			val bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
			insertImage(contentResolver, bitmap, "image", "description")
			finish()
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
			//added color change to button when clicked Trevor
			shareButton.setTextColor(Color.CYAN)
			// Save file to disk in a temp file.
			val file = File(
				getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				"temp_${System.currentTimeMillis()}.png"
			)
			val stream = FileOutputStream(file)
			val bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
			stream.close()

			// Gets the file URI from the file provider.
			val uri = FileProvider.getUriForFile(
				applicationContext, "${applicationContext.packageName}.fileprovider", file
			)

			// Share image to the system.
			val intent = Intent().apply {
				type = "image/*"
				action = Intent.ACTION_SEND
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				putExtra(Intent.EXTRA_STREAM, uri)
			}

			// Create the chooser intent.
			val chooser = Intent.createChooser(intent, "Share to")

			// Grant permission to the image so the system can access it.
			packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
				.forEach {
					grantUriPermission(
						it.activityInfo.packageName,
						uri,
						Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
					)
				}

			startActivity(chooser)
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
			//added color change to button when clicked Trevor
			exposeButton.setTextColor(Color.CYAN)
			var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
			bitmap = bitmap.copy(bitmap.config, true)
			bitmap = Exposure.doTransformation(bitmap)
			findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
			bitmap.also { bitmapModel.bitmap = it }
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
			//added color change to button when clicked Trevor
			colorBalanceButton.setTextColor(Color.CYAN)
			var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
			bitmap = bitmap.copy(bitmap.config, true)
			bitmap = ColorBalance.doTransformation(bitmap)
			findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
			bitmap.also { bitmapModel.bitmap = it }
		}
	}
}