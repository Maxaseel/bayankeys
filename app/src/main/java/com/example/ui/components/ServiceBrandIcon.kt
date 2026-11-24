package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PasswordServiceBrand

@Composable
fun ServiceBrandIcon(
    brand: PasswordServiceBrand,
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(size * 0.26f)
    val bgModifier = when (brand) {
        PasswordServiceBrand.INSTAGRAM -> Modifier.background(
            Brush.linearGradient(
                listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
            )
        )
        else -> Modifier.background(Color(brand.badgeBgArgb))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(bgModifier),
        contentAlignment = Alignment.Center
    ) {
        when (brand) {
            PasswordServiceBrand.GOOGLE -> {
                Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.2f)) {
                    val strokeWidth = this.size.minDimension * 0.22f
                    val inset = strokeWidth / 2f
                    val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
                    val topLeft = Offset(inset, inset)
                    // Blue arc + horizontal bar
                    drawArc(
                        color = Color(0xFF4285F4),
                        startAngle = -15f,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    // Green arc
                    drawArc(
                        color = Color(0xFF34A853),
                        startAngle = 55f,
                        sweepAngle = 95f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    // Yellow arc
                    drawArc(
                        color = Color(0xFFFBBC05),
                        startAngle = 148f,
                        sweepAngle = 62f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    // Red arc
                    drawArc(
                        color = Color(0xFFEA4335),
                        startAngle = 208f,
                        sweepAngle = 108f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    // G crossbar
                    drawLine(
                        color = Color(0xFF4285F4),
                        start = Offset(this.size.width * 0.52f, this.size.height * 0.5f),
                        end = Offset(this.size.width - inset * 0.3f, this.size.height * 0.5f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Butt
                    )
                }
            }
            PasswordServiceBrand.X_TWITTER -> {
                Text(
                    text = "𝕏",
                    color = Color.White,
                    fontSize = (size.value * 0.54f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            PasswordServiceBrand.GITHUB -> {
                Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.16f)) {
                    val w = this.size.width
                    val h = this.size.height
                    // White circle badge with dark ears + head silhouette
                    drawCircle(color = Color.White, radius = w * 0.46f)
                    drawCircle(
                        color = Color(0xFF111827),
                        radius = w * 0.24f,
                        center = Offset(w * 0.5f, h * 0.54f)
                    )
                    // Left & right cat ears
                    drawCircle(
                        color = Color(0xFF111827),
                        radius = w * 0.10f,
                        center = Offset(w * 0.33f, h * 0.35f)
                    )
                    drawCircle(
                        color = Color(0xFF111827),
                        radius = w * 0.10f,
                        center = Offset(w * 0.67f, h * 0.35f)
                    )
                    // Body base
                    drawOval(
                        color = Color(0xFF111827),
                        topLeft = Offset(w * 0.32f, h * 0.70f),
                        size = Size(w * 0.36f, h * 0.28f)
                    )
                }
            }
            PasswordServiceBrand.INSTAGRAM -> {
                Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.22f)) {
                    val stroke = Stroke(width = this.size.minDimension * 0.12f)
                    drawRoundRect(
                        color = Color.White,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(this.size.width * 0.28f),
                        style = stroke
                    )
                    drawCircle(
                        color = Color.White,
                        radius = this.size.minDimension * 0.23f,
                        style = stroke
                    )
                    drawCircle(
                        color = Color.White,
                        radius = this.size.minDimension * 0.07f,
                        center = Offset(this.size.width * 0.76f, this.size.height * 0.24f)
                    )
                }
            }
            PasswordServiceBrand.FACEBOOK -> {
                Text(
                    text = "f",
                    color = Color.White,
                    fontSize = (size.value * 0.62f).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            PasswordServiceBrand.MICROSOFT -> {
                Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.24f)) {
                    val gap = this.size.width * 0.1f
                    val boxW = (this.size.width - gap) / 2f
                    val boxH = (this.size.height - gap) / 2f
                    drawRect(Color(0xFFF25022), Offset(0f, 0f), Size(boxW, boxH))
                    drawRect(Color(0xFF7FBA00), Offset(boxW + gap, 0f), Size(boxW, boxH))
                    drawRect(Color(0xFF00A4EF), Offset(0f, boxH + gap), Size(boxW, boxH))
                    drawRect(Color(0xFFFFB900), Offset(boxW + gap, boxH + gap), Size(boxW, boxH))
                }
            }
            PasswordServiceBrand.APPLE -> {
                Text(
                    text = "A",
                    color = Color.White,
                    fontSize = (size.value * 0.52f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            PasswordServiceBrand.TELEGRAM -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = brand.labelEn,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.54f)
                )
            }
            PasswordServiceBrand.YOUTUBE -> {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = brand.labelEn,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.64f)
                )
            }
            PasswordServiceBrand.BANK -> {
                Icon(
                    imageVector = Icons.Rounded.AccountBalance,
                    contentDescription = brand.labelEn,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.54f)
                )
            }
            PasswordServiceBrand.CUSTOM -> {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = brand.labelEn,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(size * 0.58f)
                )
            }
        }
    }
}
