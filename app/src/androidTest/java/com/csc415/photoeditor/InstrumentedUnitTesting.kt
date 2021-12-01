package com.csc415.photoeditor

import com.csc415.photoeditor.transform.ColorBalance.doTransformation
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class InstrumentedUnitTesting {
    @Test
    fun checkColorBalance() {
        try {
            val testContext = InstrumentationRegistry.getInstrumentation().context
            val assetManager = testContext.assets
            val testInput = assetManager.open("IMG_0172_OG.png")
            //val photoshop = assetManager.open("IMG_0172-1.png")
            val original = BitmapFactory.decodeStream(testInput)
            //val photoshopi = BitmapFactory.decodeStream(photoshop)
            print("GotHere")
            val ourBitmap = doTransformation(original)
            var ogdiff = 0.0
            var altdiff = 0.0
            for (x in 0 until original.width) {
                for (y in 0 until original.height) {
                    val ogPixel = original.getPixel(x, y)
                    val altPixel = ourBitmap.getPixel(x, y)
                    ogdiff += (Math.abs(Color.red(ogPixel)) + Math.abs(Color.green(ogPixel)) + Math.abs(
                        Color.blue(ogPixel)
                    )).toDouble()
                    altdiff += (Math.abs(Color.red(altPixel)) + Math.abs(Color.green(altPixel)) + Math.abs(
                        Color.blue(altPixel)
                    )).toDouble()
                }
            }
            val diff = ogdiff / altdiff
            Assert.assertTrue(diff > 0.8 /*&& pdiff > 0.8*/)
        } catch (e: Exception) {
            print(e.message)
            e.printStackTrace()
            Assert.fail(e.stackTraceToString() + " errormessage")
        }
    }
}