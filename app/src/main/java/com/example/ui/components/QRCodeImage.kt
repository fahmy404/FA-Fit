package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@Composable
fun QRCodeImage(content: String, size: Dp = 100.dp, modifier: Modifier = Modifier) {
    val bitmap = remember(content) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val sizePx = 300 // default pixel size
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = modifier.size(size)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            // Error state or empty
        }
    }
}
