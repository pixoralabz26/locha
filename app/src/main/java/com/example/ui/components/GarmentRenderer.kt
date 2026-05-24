package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.GarmentType

@Composable
fun GarmentRenderer(
    garmentType: GarmentType,
    baseColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Clean shadow base
        drawOval(
            color = Color.Black.copy(alpha = 0.08f),
            topLeft = Offset(w * 0.15f, h * 0.90f),
            size = Size(w * 0.7f, h * 0.08f)
        )

        when (garmentType) {
            GarmentType.TEE -> drawTee(w, h, baseColor, accentColor)
            GarmentType.HOODIE -> drawHoodie(w, h, baseColor, accentColor)
            GarmentType.JACKET -> drawJacket(w, h, baseColor, accentColor)
            GarmentType.CAP -> drawCap(w, h, baseColor, accentColor)
            GarmentType.PANTS -> drawPants(w, h, baseColor, accentColor)
        }
    }
}

private fun DrawScope.drawTee(w: Float, h: Float, base: Color, accent: Color) {
    val teePath = Path().apply {
        // Neck Left
        moveTo(w * 0.38f, h * 0.15f)
        // Shoulder Left
        lineTo(w * 0.20f, h * 0.19f)
        // Sleeve Left Outer
        lineTo(w * 0.10f, h * 0.42f)
        // Sleeve Left Opening
        lineTo(w * 0.24f, h * 0.48f)
        // Underarm Left
        lineTo(w * 0.28f, h * 0.38f)
        // Torso Left
        lineTo(w * 0.28f, h * 0.85f)
        // Bottom Hem
        lineTo(w * 0.72f, h * 0.85f)
        // Torso Right
        lineTo(w * 0.72f, h * 0.38f)
        // Underarm Right
        lineTo(w * 0.76f, h * 0.48f)
        // Sleeve Right Opening
        lineTo(w * 0.90f, h * 0.42f)
        // Sleeve Right Outer
        lineTo(w * 0.80f, h * 0.19f)
        // Shoulder Right
        lineTo(w * 0.62f, h * 0.15f)
        // Neck collar curve
        quadraticTo(w * 0.5f, h * 0.22f, w * 0.38f, h * 0.15f)
        close()
    }

    // Fill garment base
    drawPath(path = teePath, color = base)

    // Draw collar border line
    val collarPath = Path().apply {
        moveTo(w * 0.38f, h * 0.15f)
        quadraticTo(w * 0.5f, h * 0.22f, w * 0.62f, h * 0.15f)
    }
    drawPath(path = collarPath, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = w * 0.025f, cap = StrokeCap.Round))

    // Draw accent pocket details / streetwear logo print on the chest
    drawRect(
        color = accent,
        topLeft = Offset(w * 0.55f, h * 0.34f),
        size = Size(w * 0.12f, h * 0.12f)
    )
    
    // Draw accent print line on sleeve
    drawLine(
        color = accent,
        start = Offset(w * 0.15f, h * 0.32f),
        end = Offset(w * 0.22f, h * 0.35f),
        strokeWidth = w * 0.02f,
        cap = StrokeCap.Round
    )

    // Outline
    drawPath(path = teePath, color = Color.Black, style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))
}

private fun DrawScope.drawHoodie(w: Float, h: Float, base: Color, accent: Color) {
    // Body & Sleeves
    val hoodiePath = Path().apply {
        moveTo(w * 0.36f, h * 0.28f)
        // Shoulders
        lineTo(w * 0.15f, h * 0.33f)
        // Long Sleeve Left Outer
        lineTo(w * 0.08f, h * 0.68f)
        // Sleeve Cuff Left
        lineTo(w * 0.17f, h * 0.70f)
        // Sleeve Inner Left
        lineTo(w * 0.26f, h * 0.46f)
        // Torso Left
        lineTo(w * 0.26f, h * 0.85f)
        // Bottom Waist hem
        lineTo(w * 0.74f, h * 0.85f)
        // Torso Right
        lineTo(w * 0.74f, h * 0.46f)
        // Sleeve Inner Right
        lineTo(w * 0.83f, h * 0.70f)
        // Sleeve Cuff Right
        lineTo(w * 0.92f, h * 0.68f)
        // Sleeve Outer Right
        lineTo(w * 0.85f, h * 0.33f)
        lineTo(w * 0.64f, h * 0.28f)
        close()
    }
    // Hood Overlay
    val hoodPath = Path().apply {
        moveTo(w * 0.34f, h * 0.28f)
        quadraticTo(w * 0.22f, h * 0.20f, w * 0.34f, h * 0.08f)
        quadraticTo(w * 0.5f, h * 0.04f, w * 0.66f, h * 0.08f)
        quadraticTo(w * 0.78f, h * 0.20f, w * 0.66f, h * 0.28f)
        quadraticTo(w * 0.5f, h * 0.35f, w * 0.34f, h * 0.28f)
    }

    // Draw Hood base and body base
    drawPath(path = hoodiePath, color = base)
    drawPath(path = hoodPath, color = base)

    // Knit Cuffs and elastic details
    val pouchPath = Path().apply {
        moveTo(w * 0.34f, h * 0.65f)
        lineTo(w * 0.66f, h * 0.65f)
        lineTo(w * 0.61f, h * 0.78f)
        lineTo(w * 0.39f, h * 0.78f)
        close()
    }
    drawPath(path = pouchPath, color = base.copy(alpha = 0.85f))
    drawPath(pouchPath, color = Color.Black, style = Stroke(width = w * 0.025f))

    // Streetwear Graphic Chest Print
    drawCircle(
        color = accent,
        radius = w * 0.06f,
        center = Offset(w * 0.5f, h * 0.48f)
    )
    
    // Draw string cords
    drawLine(
        color = accent,
        start = Offset(w * 0.44f, h * 0.32f),
        end = Offset(w * 0.42f, h * 0.45f),
        strokeWidth = w * 0.015f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = accent,
        start = Offset(w * 0.56f, h * 0.32f),
        end = Offset(w * 0.58f, h * 0.45f),
        strokeWidth = w * 0.015f,
        cap = StrokeCap.Round
    )

    // Outlines
    drawPath(path = hoodiePath, color = Color.Black, style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))
    drawPath(path = hoodPath, color = Color.Black, style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))
}

private fun DrawScope.drawJacket(w: Float, h: Float, base: Color, accent: Color) {
    val jacketPath = Path().apply {
        moveTo(w * 0.34f, h * 0.24f) // Left neck collar
        lineTo(w * 0.18f, h * 0.28f) // Left shoulder
        lineTo(w * 0.12f, h * 0.72f) // Left long sleeve
        lineTo(w * 0.22f, h * 0.72f) // Sleeve opening
        lineTo(w * 0.28f, h * 0.42f) // Armpit
        lineTo(w * 0.28f, h * 0.85f) // Left Hem
        lineTo(w * 0.72f, h * 0.85f) // Right Hem
        lineTo(w * 0.72f, h * 0.42f) // Armpit right
        lineTo(w * 0.78f, h * 0.72f) // Sleeve opening
        lineTo(w * 0.88f, h * 0.72f) // Right long sleeve outer
        lineTo(w * 0.82f, h * 0.28f) // Shoulder right
        lineTo(w * 0.66f, h * 0.24f) // Right neck collar
        close()
    }

    drawPath(path = jacketPath, color = base)

    // Zip/Buttons split lines in center
    drawLine(
        color = Color.Black,
        start = Offset(w * 0.5f, h * 0.28f),
        end = Offset(w * 0.5f, h * 0.85f),
        strokeWidth = w * 0.03f
    )

    // Collar flaps
    val leftCollar = Path().apply {
        moveTo(w * 0.34f, h * 0.24f)
        lineTo(w * 0.48f, h * 0.35f)
        lineTo(w * 0.5f, h * 0.24f)
        close()
    }
    val rightCollar = Path().apply {
        moveTo(w * 0.66f, h * 0.24f)
        lineTo(w * 0.52f, h * 0.35f)
        lineTo(w * 0.5f, h * 0.24f)
        close()
    }
    drawPath(leftCollar, color = base.copy(alpha = 0.9f))
    drawPath(leftCollar, color = Color.Black, style = Stroke(width = w * 0.025f))
    drawPath(rightCollar, color = base.copy(alpha = 0.9f))
    drawPath(rightCollar, color = Color.Black, style = Stroke(width = w * 0.025f))

    // Accent Utility chest pockets
    drawRect(
        color = accent,
        topLeft = Offset(w * 0.32f, h * 0.38f),
        size = Size(w * 0.14f, h * 0.14f)
    )
    drawRect(
        color = accent.copy(alpha = 0.8f),
        topLeft = Offset(w * 0.54f, h * 0.38f),
        size = Size(w * 0.14f, h * 0.14f)
    )

    // 3D pocket outline
    drawRect(Color.Black, topLeft = Offset(w * 0.32f, h * 0.38f), size = Size(w * 0.14f, h * 0.14f), style = Stroke(width = w * 0.025f))
    drawRect(Color.Black, topLeft = Offset(w * 0.54f, h * 0.38f), size = Size(w * 0.14f, h * 0.14f), style = Stroke(width = w * 0.025f))

    // Outlines
    drawPath(path = jacketPath, color = Color.Black, style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))
}

private fun DrawScope.drawCap(w: Float, h: Float, base: Color, accent: Color) {
    // Dome/Crown base
    val capDome = Path().apply {
        moveTo(w * 0.2f, h * 0.55f)
        cubicTo(w * 0.2f, h * 0.25f, w * 0.8f, h * 0.25f, w * 0.8f, h * 0.55f)
        close()
    }
    drawPath(path = capDome, color = base)

    // Front Visor/Brim (curves outwards)
    val capBrim = Path().apply {
        moveTo(w * 0.72f, h * 0.52f)
        quadraticTo(w * 0.92f, h * 0.58f, w * 0.86f, h * 0.68f)
        quadraticTo(w * 0.65f, h * 0.62f, w * 0.24f, h * 0.55f)
    }
    drawPath(path = capBrim, color = base.copy(alpha = 0.85f))
    drawPath(path = capBrim, color = Color.Black, style = Stroke(width = w * 0.035f))

    // Stitch lines / Panel separators
    drawLine(
        color = Color.Black.copy(alpha = 0.3f),
        start = Offset(w * 0.5f, h * 0.28f),
        end = Offset(w * 0.5f, h * 0.55f),
        strokeWidth = w * 0.02f
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.3f),
        start = Offset(w * 0.5f, h * 0.28f),
        end = Offset(w * 0.32f, h * 0.45f),
        strokeWidth = w * 0.02f
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.3f),
        start = Offset(w * 0.5f, h * 0.28f),
        end = Offset(w * 0.68f, h * 0.45f),
        strokeWidth = w * 0.02f
    )

    // Accent button on top crown
    drawCircle(
        color = accent,
        radius = w * 0.045f,
        center = Offset(w * 0.5f, h * 0.28f)
    )
    drawCircle(
        color = Color.Black,
        radius = w * 0.045f,
        center = Offset(w * 0.5f, h * 0.28f),
        style = Stroke(width = w * 0.02f)
    )

    // Brand logo rectangular embroidered tag
    drawRect(
        color = accent,
        topLeft = Offset(w * 0.38f, h * 0.44f),
        size = Size(w * 0.16f, h * 0.09f)
    )
    drawRect(
        color = Color.Black,
        topLeft = Offset(w * 0.38f, h * 0.44f),
        size = Size(w * 0.16f, h * 0.09f),
        style = Stroke(width = w * 0.02f)
    )

    // Outlines
    drawPath(path = capDome, color = Color.Black, style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))
}

private fun DrawScope.drawPants(w: Float, h: Float, base: Color, accent: Color) {
    val pantsPath = Path().apply {
        moveTo(w * 0.32f, h * 0.22f) // Waist left
        lineTo(w * 0.68f, h * 0.22f) // Waist right
        lineTo(w * 0.74f, h * 0.85f) // Right leg outline
        lineTo(w * 0.55f, h * 0.85f) // Right leg cuff inner
        lineTo(w * 0.5f, h * 0.52f)  // Crotch point
        lineTo(w * 0.45f, h * 0.85f) // Left leg cuff inner
        lineTo(w * 0.26f, h * 0.85f) // Left leg outline
        close()
    }

    drawPath(path = pantsPath, color = base)

    // Pocket Slits
    drawLine(
        color = Color.Black,
        start = Offset(w * 0.34f, h * 0.28f),
        end = Offset(w * 0.30f, h * 0.42f),
        strokeWidth = w * 0.025f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.Black,
        start = Offset(w * 0.66f, h * 0.28f),
        end = Offset(w * 0.70f, h * 0.42f),
        strokeWidth = w * 0.025f,
        cap = StrokeCap.Round
    )

    // Elastic waist ribs (accent stitching)
    for (i in 0..4) {
        val fraction = 0.34f + (0.08f * i)
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(w * fraction, h * 0.23f),
            end = Offset(w * fraction, h * 0.29f),
            strokeWidth = w * 0.015f
        )
    }

    // Modern utility label keyhanger loops
    drawLine(
        color = accent,
        start = Offset(w * 0.38f, h * 0.30f),
        end = Offset(w * 0.38f, h * 0.48f),
        strokeWidth = w * 0.02f,
        cap = StrokeCap.Round
    )

    // Outlines
    drawPath(path = pantsPath, color = Color.Black, style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))
}
