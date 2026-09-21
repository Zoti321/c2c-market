package com.zoti321.c2cmarket.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun compressToJpeg(localContentUri: Uri): Result<File> = runCatching {
        context.contentResolver.openInputStream(localContentUri)?.use { input ->
            val original = BitmapFactory.decodeStream(input)
                ?: error("Cannot decode image")
            val scaled = scaleDown(original, MAX_LONG_EDGE)
            val bytes = ByteArrayOutputStream().use { output ->
                if (!scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    error("JPEG compression failed")
                }
                output.toByteArray()
            }
            if (bytes.size > MAX_BYTES) {
                error("Image exceeds 5 MB after compression")
            }
            val tempFile = File.createTempFile("listing_", ".jpg", context.cacheDir)
            tempFile.writeBytes(bytes)
            if (original !== scaled) {
                original.recycle()
                scaled.recycle()
            }
            tempFile
        } ?: error("Cannot open image URI")
    }

    private fun scaleDown(bitmap: Bitmap, maxLongEdge: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val longEdge = max(width, height)
        if (longEdge <= maxLongEdge) return bitmap
        val scale = maxLongEdge.toFloat() / longEdge
        val targetWidth = (width * scale).toInt()
        val targetHeight = (height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    companion object {
        const val JPEG_QUALITY = 85
        const val MAX_LONG_EDGE = 1920
        const val MAX_BYTES = 5 * 1024 * 1024
    }
}
