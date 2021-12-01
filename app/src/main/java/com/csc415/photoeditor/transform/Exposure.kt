package com.csc415.photoeditor.transform

import android.graphics.Bitmap
import android.graphics.Color
import com.csc415.photoeditor.util.findWhitestPixel
import kotlin.math.min

object Exposure : ITransformation
{
	/**
	 * Fixes exposure on an image using a reference pixel found automatically.
	 *
	 * @param input The input image which will be balanced.
	 *
	 * @return The exposure balanced image.
	 *
	 * @author Will St. Onge
	 */
	override fun doTransformation(input: Bitmap): Bitmap
	{
		val whitestPixel = findWhitestPixel(input)
		return doTransformation(input, whitestPixel.first, whitestPixel.second)
	}

	/**
	 * Fixes exposure on an image with a given pixel for reference.
	 *
	 * @param input The input image which will be balanced.
	 * @param pixelX X coordinate of the pixel which will be used for reference.
	 * @param pixelY Y coordinate of the pixel which will be used for reference.
	 *
	 * @return The exposure balanced image.
	 *
	 * @author Will St. Onge
	 */
	private fun doTransformation(input: Bitmap, pixelX: Int, pixelY: Int): Bitmap
	{
		val adjust = 255 - Color.red(input.getPixel(pixelX, pixelY))

		// If the pixel is already 100% red, then we don't need to do anything.
		if (Color.red(input.getPixel(pixelX, pixelY)) == 255) return input

		val width = input.width
		val height = input.height
		val pixels = IntArray(input.width * input.height)

		input.getPixels(pixels, 0, width, 0, 0, width, height)

		for (i in 0 until input.width * input.height)
		{
			// Get the parts of the pixel's color.
			var red = Color.red(pixels[i])
			var green = Color.green(pixels[i])
			var blue = Color.blue(pixels[i])

			// Calculate the new RGB values with the adjust (max 255).
			red = min(red + adjust, 0xff)
			green = min(green + adjust, 0xff)
			blue = min(blue + adjust, 0xff)

			// Calculate the color from its parts.
			pixels[i] = (Color.alpha(pixels[i]) shl 24) or (red shl 16) or (green shl 8) or blue
		}

		input.setPixels(pixels, 0, width, 0, 0, width, height)

		return input
	}
}