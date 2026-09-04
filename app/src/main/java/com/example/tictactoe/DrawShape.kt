package com.example.tictactoe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DrawCross(modifier: Modifier = Modifier.size(70.dp), color: Color = Color.Red) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.12f
        val inset = size.minDimension * 0.22f
        val topLeft = Offset(inset, inset)
        val bottomRight = Offset(size.width - inset, size.height - inset)
        val topRight = Offset(size.width - inset, inset)
        val bottomLeft = Offset(inset, size.height - inset)

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
fun DrawCircle(modifier: Modifier = Modifier.size(70.dp), color: Color = Color.Blue) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.12f
        val radius = size.minDimension / 2 - strokeWidth / 2

        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun DrawMark(mark: Mark, modifier: Modifier = Modifier) {
    when (mark) {
        Mark.X -> DrawCross(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            color = Color(0xFFE84A5F)
        )

        Mark.O -> DrawCircle(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            color = Color(0xFF1D8A99)
        )

        Mark.Empty -> Unit
    }
}
