package com.csc415.photoeditor.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Pair
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.IllegalArgumentException

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
	if (maxHeight < 1 || maxWidth < 1)
		throw IllegalArgumentException("Height and width must be at least 1.")

	var image = bitmap
	val ratioBitmap = image.width.toFloat() / image.height.toFloat()
	val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
	var finalWidth = maxWidth
	var finalHeight = maxHeight

	if (ratioMax > ratioBitmap)
		finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
	else
		finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()

	image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)

	val outputStream = ByteArrayOutputStream()
	image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

	return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
}