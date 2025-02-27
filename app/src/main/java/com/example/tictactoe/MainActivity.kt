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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.ui.theme.TicTacToeTheme

// Win Highlighting
//Highlight the winning row, column, or diagonal when a player wins.
//Change the button background color for winning tiles.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicTacBoard()
                }
            }
        }
    }
}

@Composable
fun TicTacBoard() {

    var currentPlayer = remember { mutableStateOf("Player 1") }
    val playerTurn = remember { mutableStateOf("${currentPlayer.value} Turn") }
    val buttons = remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    val gameOver = remember { mutableStateOf(false) }
    val history = remember { mutableStateOf<List<List<MutableList<String>>>>(emptyList()) }

    fun saveState() {
        history.value = history.value + listOf(buttons.value.map { it.toMutableList() })
    }


    // Function to reset the game
    fun resetGame() {
        buttons.value = List(3) { MutableList(3) { "" } }
        currentPlayer.value = "Player 1"
        playerTurn.value = "${currentPlayer.value} Turn"
        gameOver.value = false
    }

    // Function to undo the last move
    fun undo() {
        if (history.value.isNotEmpty()) {
            // Remove the last move from history
            val lastState = history.value.last()
            history.value = history.value.dropLast(1)

            // Restore the board to the previous state
            buttons.value = lastState.map { it.toMutableList() }

            // Switch back to the previous player correctly
            currentPlayer.value = if (currentPlayer.value == "Player 1") "Player 2" else "Player 1"
            playerTurn.value = "${currentPlayer.value} Turn"

            // Ensure game is still playable
            gameOver.value = false
        }
    }

    fun onButtonClick(row: Int, col: Int, context: Context) {
        if (gameOver.value) return

        saveState()

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
            Button(onClick = {resetGame() },
                colors = ButtonDefaults.buttonColors(Color.Red), // Set the background color
                shape = MaterialTheme.shapes.small, // Set the button shape (rounded corners)
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp), // Set the button elevation for shadow
                modifier = Modifier.padding(16.dp),
            ) {
                Text("PvP");
            }
            Button(onClick = {resetGame() },
                colors = ButtonDefaults.buttonColors(Color.Blue), // Set the background color
                shape = MaterialTheme.shapes.small, // Set the button shape (rounded corners)
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp), // Set the button elevation for shadow
                modifier = Modifier.padding(16.dp),
            ) {
                Text("AI");
            }
            Button(
                onClick = {
                    (context as? ComponentActivity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(Color.Black), // Set the background color
                shape = MaterialTheme.shapes.small, // Set the button shape (rounded corners)
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp), // Set the button elevation for shadow
                modifier = Modifier.padding(16.dp),

                ) {

                Text("X", color = Color.White, style = TextStyle(fontSize = 30.sp))
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
                                enabled = !gameOver.value,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Red
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                // Display "X" or "O" as text
                                Text(
                                    text = buttons.value[rowIndex][colIndex],
                                    style = TextStyle(fontSize = 24.sp)
                                )
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
