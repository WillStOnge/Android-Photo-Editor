package com.csc415.photoeditor.transform

import android.graphics.Bitmap

object ColorInvert : ITransformation
{
	private const val RGB_MASK: Int = 0x00FFFFFF

	/**
	 * Inverts the colors of an image.
	 *
	 * @param input The input image which will be inverted.
	 *
	 * @return The inverted image.
	 *
	 * @author Shawn Huesman
	 */
	override fun doTransformation(input: Bitmap): Bitmap
	{
		val inversionBitmap: Bitmap = input.copy(Bitmap.Config.ARGB_8888, true)

		val width: Int = inversionBitmap.width
		val height: Int = inversionBitmap.height
		val pixelCount: Int = width * height

		val pixels = IntArray(pixelCount)
		inversionBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

		for (i in 0 until pixelCount) pixels[i] = pixels[i] xor RGB_MASK

		inversionBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

		return inversionBitmap
	}
}