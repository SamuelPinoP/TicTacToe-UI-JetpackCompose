package com.example.tictactoe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DrawCross(modifier: Modifier = Modifier, color: Color = Color.Red) {
    Canvas(modifier = modifier.size(30.dp)) {
        val strokeWidth = 10f
        val topLeft = Offset(0f, 0f)
        val bottomRight = Offset(size.width, size.height)
        val topRight = Offset(size.width, 0f)
        val bottomLeft = Offset(0f, size.height)

        drawLine(
            color = color,
            start = topLeft,
            end = bottomRight,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = topRight,
            end = bottomLeft,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}


@Composable
fun DrawCircle(modifier: Modifier = Modifier, color: Color = Color.Blue) {
    Canvas(modifier = modifier.size(70.dp)) {
        val strokeWidth = 10f
        val radius = size.minDimension / 2 - strokeWidth / 2

        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )
    }
}
