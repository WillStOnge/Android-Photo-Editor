package com.csc415.photoeditor.transform

import android.graphics.Bitmap

sealed interface ITransformation
{
	fun doTransformation(input: Bitmap): Bitmap
}