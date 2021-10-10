package com.csc415.photoeditor.transform

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.min

object Exposure : ITransformation
{
	/**
	 * Fixes exposure on an image using a reference pixel found automatically.
	 *
	 * @param input The input image which will be balanced.
	 *
	 * @return The exposure balanced image.
	 */
	override fun doTransformation(input: Bitmap): Bitmap
	{
		// Need to figure out how to auto select a pixel for reference.

		return doTransformation(input, 0, 0);
	}

	/**
	 * Fixes exposure on an image with a given pixel for reference.
	 *
	 * @param input The input image which will be balanced.
	 * @param pixelX X coordinate of the pixel which will be used for reference.
	 * @param pixelY Y coordinate of the pixel which will be used for reference.
	 *
	 * @return The exposure balanced image.
	 */
	fun doTransformation(input: Bitmap, pixelX: Int, pixelY: Int): Bitmap
	{
		val adjust = Color.red(input.getPixel(pixelX, pixelY))

		// If the pixel is already fully red, then we don't need to do anything.
		if (adjust == 255)
			return input

		for (x in 1..input.width)
		{
			for (y in 1..input.height)
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

				// Calculate the color from the parts.
				input.setPixel(x, y, (alpha shl 24) or (red shl 16) or (green shl 8) or blue)
			}
		}

		return input
	}
}