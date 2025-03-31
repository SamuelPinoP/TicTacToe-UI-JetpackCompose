package com.example.tictactoe

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.ui.theme.TicTacToeTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


// Highlight the winning row, column, or diagonal when a player wins.


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE0F7FA)
                    // color = MaterialTheme.colorScheme.background
                ) {
                    TicTacBoard()
                }
            }
        }
    }
}
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

@Composable
fun TicTacBoard() {

    var currentPlayer = remember { mutableStateOf("Player 1") }
    val playerTurn = remember { mutableStateOf("${currentPlayer.value} Turn") }
    val buttons = remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    val gameOver = remember { mutableStateOf(false) }
    val history = remember { mutableStateOf<List<List<MutableList<String>>>>(emptyList()) }
    var isAI = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(key1 = isAI.value, key2 = currentPlayer.value, key3 = gameOver.value) {
        if (isAI.value && currentPlayer.value == "Player 2" && !gameOver.value) {
            delay(500) // Simulate AI "thinking"

            val emptyCells = mutableListOf<Pair<Int, Int>>()
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    if (buttons.value[row][col].isEmpty()) {
                        emptyCells.add(row to col)
                    }
                }
            }

            if (emptyCells.isNotEmpty()) {
                val (row, col) = emptyCells.random()
                buttons.value[row][col] = "X"
                currentPlayer.value = "Player 1"
                playerTurn.value = "${currentPlayer.value} Turn"

                if ((buttons.value[0][0] == buttons.value[0][1] && buttons.value[0][1] == buttons.value[0][2] && buttons.value[0][0].isNotEmpty()) ||
                    (buttons.value[1][0] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[1][2] && buttons.value[1][0].isNotEmpty()) ||
                    (buttons.value[2][0] == buttons.value[2][1] && buttons.value[2][1] == buttons.value[2][2] && buttons.value[2][0].isNotEmpty()) ||
                    (buttons.value[0][0] == buttons.value[1][0] && buttons.value[1][0] == buttons.value[2][0] && buttons.value[0][0].isNotEmpty()) ||
                    (buttons.value[0][1] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[2][1] && buttons.value[0][1].isNotEmpty()) ||
                    (buttons.value[0][2] == buttons.value[1][2] && buttons.value[1][2] == buttons.value[2][2] && buttons.value[0][2].isNotEmpty()) ||
                    (buttons.value[0][0] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[2][2] && buttons.value[0][0].isNotEmpty()) ||
                    (buttons.value[2][0] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[0][2] && buttons.value[2][0].isNotEmpty())) {

                    gameOver.value = true // Set game over state to true
                    Toast.makeText(context, "AI is the winnner!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun saveState() {
        history.value = history.value + listOf(buttons.value.map { it.toMutableList() })
    }


    fun resetGame() {
        buttons.value = List(3) { MutableList(3) { "" } }
        currentPlayer.value = "Player 1"
        playerTurn.value = "${currentPlayer.value} Turn"
        gameOver.value = false
    }


    fun undo() {
        if (isAI.value == true){
            Toast.makeText(context, "Cannot undo against AI!", Toast.LENGTH_SHORT).show()
        }
        if (history.value.isNotEmpty() && isAI.value == false){
            val lastState = history.value.last()
            history.value = history.value.dropLast(1)

            buttons.value = lastState.map { it.toMutableList() }

            currentPlayer.value = if (currentPlayer.value == "Player 1") "Player 2" else "Player 1"
            playerTurn.value = "${currentPlayer.value} Turn"

            gameOver.value = false
        }
    }

    fun onButtonClick(row: Int, col: Int, context: Context) {
        if (gameOver.value) return

        saveState()

        if (isAI.value == true && currentPlayer.value == "Player 2"){
            return
        }

        if (buttons.value[row][col].isEmpty()) {

            buttons.value[row][col] = if (currentPlayer.value == "Player 1") "0" else "X"
            currentPlayer.value = if (currentPlayer.value == "Player 1") "Player 2" else "Player 1"
            playerTurn.value = "${currentPlayer.value} Turn"


            if ((buttons.value[0][0] == buttons.value[0][1] && buttons.value[0][1] == buttons.value[0][2] && buttons.value[0][0].isNotEmpty()) ||
                (buttons.value[1][0] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[1][2] && buttons.value[1][0].isNotEmpty()) ||
                (buttons.value[2][0] == buttons.value[2][1] && buttons.value[2][1] == buttons.value[2][2] && buttons.value[2][0].isNotEmpty()) ||
                (buttons.value[0][0] == buttons.value[1][0] && buttons.value[1][0] == buttons.value[2][0] && buttons.value[0][0].isNotEmpty()) ||
                (buttons.value[0][1] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[2][1] && buttons.value[0][1].isNotEmpty()) ||
                (buttons.value[0][2] == buttons.value[1][2] && buttons.value[1][2] == buttons.value[2][2] && buttons.value[0][2].isNotEmpty()) ||
                (buttons.value[0][0] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[2][2] && buttons.value[0][0].isNotEmpty()) ||
                (buttons.value[2][0] == buttons.value[1][1] && buttons.value[1][1] == buttons.value[0][2] && buttons.value[2][0].isNotEmpty())) {

                gameOver.value = true // Set game over state to true

                if ("${currentPlayer.value}" == "Player 1"){
                    Toast.makeText(context, "Player 2 is the winnner!", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(context, "Player 1 is the winnner!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(), // Fill the available screen space
        verticalArrangement = Arrangement.Center, // Arrange rows at the top
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
    ) {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween // Places buttons at Start, Center, and End
        ) {
            Button(
                onClick = {
                    resetGame()
                    isAI.value = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isAI.value == false) Color.Green else Color.Red),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Text("PvP", style = TextStyle(fontSize = 25.sp))
            }
            Button(
                onClick = {
                    resetGame()
                    isAI.value = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isAI.value) Color.Green else Color.Blue),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Text("AI", style = TextStyle(fontSize = 25.sp))
            }
            Button(
                onClick = {
                    (context as? ComponentActivity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                modifier = Modifier.padding(16.dp),

                ) {

                Text("X", color = Color.White, style = TextStyle(fontSize = 25.sp))
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text("Tic Tac Toe", color = Color.Blue, style = TextStyle(fontSize = 30.sp))

        Spacer(modifier = Modifier.height(50.dp))

        Text(playerTurn.value, style = TextStyle(fontSize = 24.sp))

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (rowIndex in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (colIndex in 0 until 3) {
                            val context = LocalContext.current
                            Button(
                                onClick = { onButtonClick(rowIndex, colIndex, context) },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .width(80.dp)
                                    .height(80.dp),
                                enabled = buttons.value[rowIndex][colIndex].isEmpty() && !gameOver.value,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Red
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                // Display "X" or "O" as text
                                when (buttons.value[rowIndex][colIndex]) {
                                    "0" -> DrawCircle(color = Color.Blue) // Draw "O"
                                    "X" -> DrawCross(color = Color.Red) // Draw "X"
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { resetGame() },
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
            ) {
                Text("↻", color = Color.Black, style = TextStyle(fontSize = 30.sp))
            }
            Button(
                onClick = { undo() },
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
            ) {
                Text("Undo", color = Color.Black, style = TextStyle(fontSize = 30.sp))
            }
        }
    }
}
