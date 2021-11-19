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

    fun resetButtonClicks() {
        EXPOSED_COUNT = 0
        BALANCED_COUNT = 0
        INVERSION_COUNT = 0
    }

    companion object {
        var EXPOSED_COUNT = 0
        var BALANCED_COUNT = 0
        var INVERSION_COUNT = 0
    }
}