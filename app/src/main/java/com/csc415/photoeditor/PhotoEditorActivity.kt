package com.csc415.photoeditor

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView

class PhotoEditorActivity : AppCompatActivity()
{
	private val TAG = PhotoEditorActivity::class.java.simpleName

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo_editor)

		// Get intent extra for the photo uri.
		if (intent.extras!!.containsKey(IntentExtraMessage.PHOTO_URI.extraName))
		{
			// Photo URI was sent.
			val imageUri = intent.getStringExtra(IntentExtraMessage.PHOTO_URI.extraName)
			Log.d(TAG, imageUri.toString())
			findViewById<ImageView>(R.id.photo).setImageURI(Uri.parse(imageUri))
		}
		else // Invalid intent.
		{
			Log.w(TAG, "No IMAGE_URI was sent with Intent, falling back to MainActivity")
			startActivity(Intent(this, MainActivity::class.java))
		}

		setupExitButton()
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
}