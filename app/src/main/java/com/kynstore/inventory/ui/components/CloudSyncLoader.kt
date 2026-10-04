package com.kynstore.inventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Cloud Sync Loader inspired by Uiverse.io (by andrew-manzyk)
 *
 * Features:
 * - Ambient animated speed lines streaming at -65 degrees
 * - Dynamic cloud puffs morphing across (20,60,r15 -> 50,45,r20 -> 80,60,r15) with phase delays
 * - Infinite 360° rotating sync reload arrows (1.0s loop)
 * - Styled in crisp #4387f4 (cloud) and #80b1ff / white (arrows)
 */
@Composable
fun CloudSyncLoader(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    cloudColor: Color = Color(0xFF4387F4),
    arrowsColor: Color = Color(0xFF80B1FF),
    linesColor: Color = Color(0xFF80B1FF).copy(alpha = 0.5f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "uiverse_cloud_loader")

    // Rotation: 0 to 360 deg in 1.0s (linear)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrows_rotation"
    )

    // Cloud morphing cycle: 0f to 1f in 2.0s
    val cloudProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_morph"
    )

    // Streaming wind lines: -10px to +8px in ~750ms
    val linesOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wind_lines"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .testTag("cloud_sync_loader")
    ) {
        val w = this.size.width
        val h = this.size.height
        val scale = w / 100f // Designed on a 100x100 canvas coordinate system

        // 1. Draw Streaming Wind Lines behind the cloud (rotated -65 degrees)
        drawWindLines(linesOffset, scale, linesColor)

        // 2. Base Cloud Silhouette & Pill
        val baseLeft = 16f * scale
        val baseTop = 50f * scale
        val baseWidth = 68f * scale
        val baseHeight = 28f * scale
        drawRoundRect(
            color = cloudColor,
            topLeft = Offset(baseLeft, baseTop),
            size = Size(baseWidth, baseHeight),
            cornerRadius = CornerRadius(14f * scale)
        )

        // 3. Draw the 3 morphing cloud puff circles with phase delays (-2/3, -1/3, 0)
        drawCloudPuff(progress = cloudProgress, scale = scale, color = cloudColor)
        drawCloudPuff(progress = (cloudProgress + 0.333f) % 1f, scale = scale, color = cloudColor)
        drawCloudPuff(progress = (cloudProgress + 0.666f) % 1f, scale = scale, color = cloudColor)

        // Extra anchoring left and right cloud bumps for full puffy silhouette
        drawCircle(
            color = cloudColor,
            radius = 16f * scale,
            center = Offset(30f * scale, 62f * scale)
        )
        drawCircle(
            color = cloudColor,
            radius = 15f * scale,
            center = Offset(70f * scale, 62f * scale)
        )

        // 4. Rotating Sync Loop Arrows (transform-origin 50% 72.89% in CSS)
        val arrowOrigin = Offset(50f * scale, 64f * scale)
        val arrowRadius = 11f * scale

        rotate(degrees = rotationAngle, pivot = arrowOrigin) {
            // Semi-transparent drop glow
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = arrowRadius + 4f * scale,
                center = arrowOrigin
            )

            // Sync Arc 1 (Top-Right)
            drawArc(
                color = arrowsColor,
                startAngle = 20f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(arrowOrigin.x - arrowRadius, arrowOrigin.y - arrowRadius),
                size = Size(arrowRadius * 2, arrowRadius * 2),
                style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
            )
            // Arrow Head 1
            drawArrowHead(
                tip = Offset(
                    arrowOrigin.x + arrowRadius * cos((150f * PI / 180f).toFloat()),
                    arrowOrigin.y + arrowRadius * sin((150f * PI / 180f).toFloat())
                ),
                angleDegrees = 240f,
                scale = scale,
                color = arrowsColor
            )

            // Sync Arc 2 (Bottom-Left)
            drawArc(
                color = arrowsColor,
                startAngle = 200f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(arrowOrigin.x - arrowRadius, arrowOrigin.y - arrowRadius),
                size = Size(arrowRadius * 2, arrowRadius * 2),
                style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
            )
            // Arrow Head 2
            drawArrowHead(
                tip = Offset(
                    arrowOrigin.x + arrowRadius * cos((330f * PI / 180f).toFloat()),
                    arrowOrigin.y + arrowRadius * sin((330f * PI / 180f).toFloat())
                ),
                angleDegrees = 60f,
                scale = scale,
                color = arrowsColor
            )
        }
    }
}

/**
 * Calculates and draws an animated cloud puff circle according to keyframes:
 * 0%: cx: 20, cy: 60, r: 15
 * 50%: cx: 50, cy: 45, r: 20
 * 100%: cx: 80, cy: 60, r: 15
 */
private fun DrawScope.drawCloudPuff(progress: Float, scale: Float, color: Color) {
    val cx: Float
    val cy: Float
    val r: Float

    if (progress <= 0.5f) {
        val t = progress / 0.5f
        cx = 20f + (50f - 20f) * t
        cy = 60f + (45f - 60f) * t
        r = 15f + (20f - 15f) * t
    } else {
        val t = (progress - 0.5f) / 0.5f
        cx = 50f + (80f - 50f) * t
        cy = 45f + (60f - 45f) * t
        r = 20f + (15f - 20f) * t
    }

    drawCircle(
        color = color,
        radius = r * scale,
        center = Offset(cx * scale, cy * scale)
    )
}

/**
 * Draws streaming wind lines with -65 degree rotation
 */
private fun DrawScope.drawWindLines(offset: Float, scale: Float, color: Color) {
    val center = Offset(50f * scale, 50f * scale)
    rotate(degrees = -65f, pivot = center) {
        val linePositions = listOf(
            Triple(25f, 15f, 30f),
            Triple(50f, 10f, 40f),
            Triple(75f, 20f, 32f)
        )
        for ((x, startY, len) in linePositions) {
            val y1 = (startY + offset) * scale
            val y2 = y1 + (len * scale)
            drawLine(
                color = color,
                start = Offset(x * scale, y1),
                end = Offset(x * scale, y2),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draws an arrowhead triangle for the sync arrows
 */
private fun DrawScope.drawArrowHead(tip: Offset, angleDegrees: Float, scale: Float, color: Color) {
    val rad = angleDegrees * PI / 180.0
    val headLen = 6f * scale
    val wingAngle = 35.0 * PI / 180.0

    val p1 = Offset(
        (tip.x - headLen * cos(rad - wingAngle)).toFloat(),
        (tip.y - headLen * sin(rad - wingAngle)).toFloat()
    )
    val p2 = Offset(
        (tip.x - headLen * cos(rad + wingAngle)).toFloat(),
        (tip.y - headLen * sin(rad + wingAngle)).toFloat()
    )

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * Fullscreen or Modal Loading Overlay featuring the Cloud Sync Loader
 * Ideal for database sync, backup/restore, and heavy operations.
 */
@Composable
fun CloudSyncLoadingOverlay(
    isLoading: Boolean,
    message: String = "Naglo-load ang datos...",
    subMessage: String = "Mangyaring maghintay...",
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Dialog(
            onDismissRequest = { /* Non-cancellable while loading */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                modifier = modifier.padding(16.dp).testTag("cloud_sync_loading_dialog")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
                ) {
                    CloudSyncLoader(
                        size = 110.dp,
                        cloudColor = Color(0xFF4387F4),
                        arrowsColor = Color(0xFF80B1FF),
                        linesColor = Color(0xFF80B1FF).copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = subMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
