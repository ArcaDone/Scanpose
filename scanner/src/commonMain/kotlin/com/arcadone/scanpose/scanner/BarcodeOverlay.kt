package com.arcadone.scanpose.scanner

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeOverlay(
    rectSizeWidth: Dp = 300.dp,
    rectSizeHeight: Dp = 200.dp,
) {
    val scanLinePosition = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scanLinePosition.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val cornerRadius = 16.dp
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw the dark overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size,
            )
            // Clear the rectangle area for scanning with rounded corners
            val rectLeft = (size.width - rectSizeWidth.toPx()) / 2
            val rectTop = (size.height - rectSizeHeight.toPx()) / 2
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(rectLeft, rectTop),
                size = Size(rectSizeWidth.toPx(), rectSizeHeight.toPx()),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                blendMode = BlendMode.Clear,
            )
        }

        Column(
            modifier = Modifier
                .size(width = rectSizeWidth, height = rectSizeHeight)
                .border(2.dp, Color.White, shape = RoundedCornerShape(cornerRadius)),
            verticalArrangement = Arrangement.Center,
        ) {
            // Content
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarcodeOverlayPreview() {
    Scaffold(containerColor = Color.White) {
        BarcodeOverlay(
        )
    }
}
