package com.csc415.photoeditor.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Pair
import android.widget.Toast
import java.io.*

/**
 * Finds the whitest pixel so we can automatically expose the image.
 *
 * @param input The input image to find the whitest pixel in.
 *
 * @return A pair of the x and y coordinates of the optimal pixel, [x, y].
 *
 * @author Will St. Onge
 */
fun findWhitestPixel(input: Bitmap): Pair<Int, Int>
{
	var pixelCoordinates = Pair(0, 0)
	var average = 0.0

	val width = input.width
	val height = input.height
	val pixels = IntArray(input.width * input.height)

	input.getPixels(pixels, 0, width, 0, 0, width, height)

	// Go through each pixel in the image to find the whitest pixel.
	for (i in 0 until input.width * input.height)
	{
		val red = Color.red(pixels[i])
		val green = Color.green(pixels[i])
		val blue = Color.blue(pixels[i])

		// Whiter pixel is found.
		if (average < (red + green + blue) / 3.0)
		{
			val x = (i % width) + 1
			val y = (i / width) + 1
			average = (red + green + blue) / 3.0
			pixelCoordinates = Pair(x, y)
		}
	}

	return pixelCoordinates
}

/**
 * Saves a file by first checking the permissions to the external storage. If the permission to
 * external storage is set, the image will be saved, otherwise a dialog appears asking permission to
 * access the external storage.
 *
 * @param bitmap The bitmap object to save.
 * @param context The context in which the image is saved (typically the calling activity).
 *
 * @author Anthony Bosch
 */
fun saveToInternalStorage(bitmap: Bitmap, context: Context)
{
	val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${System.currentTimeMillis()}.png")
	FileOutputStream(file).use { stream ->
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
	}

	Toast.makeText(context, "Saved file!", Toast.LENGTH_SHORT).show()
}
  
/**
 * Scales and compresses a Bitmap while preserving the aspect ratio of the image.
 *
 * @param stream An input stream containing an image which will be compressed.
 * @param maxHeight Max height of the resulting image.
 * @param maxWidth Max width of the resulting image.
 *
 * @return The compressed Bitmap.
 *
 * @author Will St. Onge
 */
fun compressImage(stream: InputStream, maxWidth: Int, maxHeight: Int): Bitmap
{
	return compressImage(BitmapFactory.decodeStream(stream), maxWidth, maxHeight)
}

/**
 * Scales and compresses a Bitmap while preserving the aspect ratio of the image.
 *
 * @param bitmap The image which will be compressed.
 * @param maxHeight Max height of the resulting image.
 * @param maxWidth Max width of the resulting image.
 *
 * @return The compressed Bitmap.
 *
 * @author Will St. Onge
 */
fun compressImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap
{
	if (maxHeight < 1 || maxWidth < 1) throw IllegalArgumentException("Height and width must be at least 1.")

	var image = bitmap
	val ratioBitmap = image.width.toFloat() / image.height.toFloat()
	val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
	var finalWidth = maxWidth
	var finalHeight = maxHeight

	if (ratioMax > ratioBitmap) finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
	else finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()

	image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)

	val outputStream = ByteArrayOutputStream()
	image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

	return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
}

/**
 * Saves a file to the gallery by setting values specific to the image and using the ContentResolver
 * to create a URI where the image will be stored.
 *
 * @param cr The ContentResolver that is being passed from the PhotoEditorActivity
 * @param source The bitmap that is passed from the PhotoEditorActivity
 * @param title Title of the new image to be stored in the gallery
 * @param description Description of the new image to be stored in the gallery
 *
 * @author Anthony Bosch
 */
fun insertImage(cr: ContentResolver, source: Bitmap?, title: String?, description: String?): String? {
	val values = setContentValues(title, description)

	var url: Uri? = null
	var stringUrl: String? = null /* value to be returned */
	try {
		url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
		if (source != null) {
			val imageOut = cr.openOutputStream(url!!)
			try {
				source.compress(Bitmap.CompressFormat.PNG, 100, imageOut)
			} finally {
				imageOut!!.close()
			}
		} else {
			cr.delete(url!!, null, null)
			url = null
		}
	} catch (e: Exception) {
		if (url != null) {
			cr.delete(url, null, null)
			url = null
		}
	}
	if (url != null) {
		stringUrl = url.toString()
	}
	return stringUrl
}


/**
 * Sets the values for a specific bitmap
 *
 * @param title
 * @param description
 *
 * @author Anthony Bosch
 */
private fun setContentValues(title: String?, description: String?): ContentValues {
	val values = ContentValues()
	values.put(MediaStore.Images.Media.TITLE, title)
	values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
	values.put(MediaStore.Images.Media.DESCRIPTION, description)
	values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
	// Add the date meta data to ensure the image is added at the front of the gallery
	values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
	values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

	return values
}