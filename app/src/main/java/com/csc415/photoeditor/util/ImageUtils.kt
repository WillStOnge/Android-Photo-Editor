package com.csc415.photoeditor.util

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Finds the whitest pixel so we can automatically expose the image.
 *
 * @param input The input image which will be balanced.
 *
 * @return An array of the x and y coordinates of the optimal pixel, [x, y].
 *
 * @author Will St. Onge
 */
fun findWhitestPixel(input: Bitmap): Pair<Int, Int>
{
	var pixels = Pair(0, 0)
	var average = 0.0

	// Go through each pixel in the image to find the whitest pixel.
	for (x in 1..input.width)
	{
		for (y in 1..input.height)
		{
			val red = Color.red(input.getPixel(x, y))
			val green = Color.green(input.getPixel(x, y))
			val blue = Color.blue(input.getPixel(x, y))

			// Whiter pixel is found.
			if (average < (red + green + blue) / 3.0)
			{
				average = (red + green + blue) / 3.0
				pixels = Pair(x, y)
			}
		}
	}

	return pixels
}