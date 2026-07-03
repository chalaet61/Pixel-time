package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    intensity: Float = 0.6f,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    // Glassmorphic translucent colors based on dark theme
    val cardBackground = Color(0xFFFFFFFF).copy(alpha = 0.07f * intensity)
    val cardBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.15f * intensity),
            Color(0xFFFFFFFF).copy(alpha = 0.02f * intensity)
        )
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = Color.Transparent,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = (8 * intensity).dp
    ) {
        Box(
            modifier = Modifier
                .background(cardBackground)
                .padding(16.dp),
            content = content
        )
    }
}
