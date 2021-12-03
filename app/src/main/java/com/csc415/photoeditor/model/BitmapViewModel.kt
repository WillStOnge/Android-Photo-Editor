package com.csc415.photoeditor.model

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

/**
 * View model so we can save the bitmap during a configuration change (ie orientation change).
 *
 * @author Will St. Onge
 */
class BitmapViewModel : ViewModel()
{
	var bitmap: Bitmap? = null
	var originalImage: Bitmap? = null
}