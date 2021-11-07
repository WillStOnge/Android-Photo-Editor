package com.csc415.photoeditor.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.csc415.photoeditor.REQUEST_CODE
import java.io.File
import java.io.FileOutputStream

/**
 * Finds the whitest pixel so we can automatically expose the image.
 *
 * @param input The input image which will be balanced.
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

fun saveToInternalStorage(bitmap: Bitmap, context: Context) {
	if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
		saveImage(bitmap, context)
	} else {
		askPermission(context)
	}
}

private fun saveImage(bitmap: Bitmap, context: Context) {
	val dir = File(Environment.getExternalStorageDirectory(), "SaveImage")

	if (!dir.exists()) {
		dir.mkdir()
	}
	val ts = System.currentTimeMillis()
	val file = File(dir, "$ts.jpg")
	val stream = FileOutputStream(file).use { stream ->
		bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
	}

	Toast.makeText(context, "Saved file!", Toast.LENGTH_SHORT).show()
}

private fun askPermission(context: Context) {
	ActivityCompat.requestPermissions(
		context as Activity,
		arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
		REQUEST_CODE
	)
}