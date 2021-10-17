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
		val adjust = Color.red(input.getPixel(pixelX, pixelY))

		// If the pixel is already 100% red, then we don't need to do anything.
		if (adjust == 255)
			return input

		for (x in 0 until input.width)
		{
			for (y in 0 until input.height)
			{
				// Get the parts of the pixel's color.
				val alpha = Color.alpha(input.getPixel(x, y))
				var red = Color.red(input.getPixel(x, y))
				var green = Color.green(input.getPixel(x, y))
				var blue = Color.blue(input.getPixel(x, y))

				// Calculate the new RGB values with the adjust (max 255).
				red = min(red + adjust, 0xff)
				green = min(green + adjust, 0xff)
				blue = min(blue + adjust, 0xff)

				// Calculate the color from the parts ((a << 24) | (r << 16) | (g << 8) | b).
				input.setPixel(x, y, (alpha shl 24) or (red shl 16) or (green shl 8) or blue)
			}
		}

		return input
	}
}