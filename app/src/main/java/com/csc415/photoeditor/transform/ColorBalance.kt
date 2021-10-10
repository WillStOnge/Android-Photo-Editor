package com.csc415.photoeditor.transform

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max

object ColorBalance : ITransformation
{
	override fun doTransformation(input: Bitmap): Bitmap
	{
		return doTransformation(input, 0, 0)
	}

	/**
	 * Fixes the color balance on an image with a given pixel for reference.
	 *
	 * @param input The input image which will be balanced.
	 * @param pixelX X coordinate of the pixel which will be used for reference.
	 * @param pixelY Y coordinate of the pixel which will be used for reference.
	 *
	 * @return The balanced image.
	 */
	fun doTransformation(input: Bitmap, pixelX: Int, pixelY: Int): Bitmap
	{
		var red = Color.red(input.getPixel(pixelX, pixelY))
		var blue = Color.blue(input.getPixel(pixelX, pixelY))
		var green = Color.green(input.getPixel(pixelX, pixelY))

		// If the color balance is equal, then skip this step.
		if (red == blue && blue == green)
			return input

		var lowColor = red

		if (red > blue) lowColor = blue
		if (red > green) lowColor = green

		// Find the difference between lowest color and each primary color.
		// We use that value to adjust the image color.
		red -= lowColor
		blue -= lowColor
		green -= lowColor

		// Apply color differences to all pixes in the image.
		for (x in 1..input.width)
		{
			for (y in 1..input.height)
			{
				// Get the current color of each pixel.
				val alpha = Color.alpha(input.getPixel(x, y))
				var currentRed = Color.red(input.getPixel(x, y))
				var currentGreen = Color.green(input.getPixel(x, y))
				var currentBlue = Color.blue(input.getPixel(x, y))

				// Calculate the new RGB values with the adjust (min 0)
				currentRed = max(currentRed - red, 0)
				currentBlue = max(currentBlue - blue, 0)
				currentGreen = max(currentGreen - green, 0)

				// Calculate the color from the parts.
				input.setPixel(x, y, (alpha shl 24) or (currentRed shl 16) or (currentGreen shl 8) or currentBlue)
			}
		}
		return input
	}
}