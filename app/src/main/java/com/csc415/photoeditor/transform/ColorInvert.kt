package com.csc415.photoeditor.transform

import android.graphics.Bitmap

object ColorInvert : ITransformation
{
	private var RGB_MASK: Int = 0x00FFFFFF

	override fun doTransformation(input: Bitmap): Bitmap
	{
		var inversionBitmap: Bitmap = input.copy(Bitmap.Config.ARGB_8888, true)

		val width: Int = inversionBitmap.width
		val height: Int = inversionBitmap.height
		val pixelCount: Int = width * height

		var pixels = IntArray(pixelCount)
		inversionBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

		for (i in 0..pixelCount) {
			pixels[i] = pixels[i] xor RGB_MASK
		}
		inversionBitmap.setPixels(pixels, 0, width, 0,0, width, height)

		return inversionBitmap
	}
}