package com.example.tictactoe

import android.os.CountDownTimer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import com.example.tictactoe.ui.theme.TicTacToeTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay

/**
 * Checks if there's a winning combination on the board
 * @param buttons The current state of the game board
 * @return Boolean indicating if a player has won
 */
private fun checkWin(buttons: List<List<String>>): Boolean {
    // Check rows
    for (row in 0 until 3) {
        if (buttons[row][0].isNotEmpty() &&
            buttons[row][0] == buttons[row][1] &&
            buttons[row][1] == buttons[row][2]) {
            return true
        }
    }

    // Check columns
    for (col in 0 until 3) {
        if (buttons[0][col].isNotEmpty() &&
            buttons[0][col] == buttons[1][col] &&
            buttons[1][col] == buttons[2][col]) {
            return true
        }
    }

    // Check diagonals
    if (buttons[0][0].isNotEmpty() &&
        buttons[0][0] == buttons[1][1] &&
        buttons[1][1] == buttons[2][2]) {
        return true
    }

    if (buttons[0][2].isNotEmpty() &&
        buttons[0][2] == buttons[1][1] &&
        buttons[1][1] == buttons[2][0]) {
        return true
    }

    return false
}

/**
 * Checks if the game has ended in a draw (no empty spaces left)
 * @param buttons The current state of the game board
 * @return Boolean indicating if the game is a draw
 */
private fun checkDraw(buttons: List<List<String>>): Boolean {
    return buttons.all { row -> row.all { it.isNotEmpty() } }
}

/**
 * Main composable function for the Tic Tac Toe game board
 */
@Composable
fun TicTacBoard() {
    // Game state variables
    var timeLeft by remember { mutableStateOf(30) }     // Countdown timer value
    var isRunning by remember { mutableStateOf(false) }     // Timer running state

    var currentPlayer = remember { mutableStateOf("Player 1") }     // Current player turn
    val playerTurn = remember { mutableStateOf("${currentPlayer.value} Turn") }     // Display text for turn
    val buttons = remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }        // 3x3 game board
    val gameOver = remember { mutableStateOf(false) }       // Game over state
    val history = remember { mutableStateOf<List<List<MutableList<String>>>>(emptyList()) }     // Move history for undo
    var isAI = remember { mutableStateOf(false) }       // Whether playing against AI

    val context = LocalContext.current

    // Timer implementation
    val timer = remember {
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                if (gameOver.value){
                    isRunning = false;
                }
                timeLeft = 0
                isRunning = false
                Toast.makeText(context, "Time's up!", Toast.LENGTH_SHORT).show()

                // Determine winner when time runs out
                if(timeLeft == 0 && !isAI.value) {
                    gameOver.value = true
                    if (currentPlayer.value == "Player 1") {
                        Toast.makeText(context, "Player 2 is the winner!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Player 1 is the winner!", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(timeLeft == 0 && isAI.value) {
                    gameOver.value = true
                    Toast.makeText(context, "AI is the winner!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Starts or resets the game timer
     */
    fun startTimer() {
        if(gameOver.value == false) {
            timer.start()
            isRunning = true
        }
        else{
            timer.cancel()
            isRunning = false
            timeLeft = 30
        }
    }

    /**
     * Finds a winning move for the specified player
     * @param board Current game board state
     * @param player The player to check for ("X" or "0")
     * @return Pair of (row, col) for the winning move, or null if none exists
     */
    fun findWinningMove(board: List<List<String>>, player: String): Pair<Int, Int>? {
        // Check rows
        for (row in 0 until 3) {
            val line = listOf(board[row][0], board[row][1], board[row][2])
            if (line.count { it == player } == 2 && line.contains("")) {
                val col = line.indexOf("")
                return row to col
            }
        }

        // Check columns
        for (col in 0 until 3) {
            val line = listOf(board[0][col], board[1][col], board[2][col])
            if (line.count { it == player } == 2 && line.contains("")) {
                val row = line.indexOf("")
                return row to col
            }
        }

        // Check diagonals
        val diag1 = listOf(board[0][0], board[1][1], board[2][2])
        if (diag1.count { it == player } == 2 && diag1.contains("")) {
            val index = diag1.indexOf("")
            return index to index
        }

        val diag2 = listOf(board[0][2], board[1][1], board[2][0])
        if (diag2.count { it == player } == 2 && diag2.contains("")) {
            val index = diag2.indexOf("")
            return when (index) {
                0 -> 0 to 2
                1 -> 1 to 1
                2 -> 2 to 0
                else -> null
            }
        }

        return null
    }

    // AI move logic
    LaunchedEffect(key1 = isAI.value, key2 = currentPlayer.value, key3 = gameOver.value) {
        if (isAI.value && currentPlayer.value == "Player 2" && !gameOver.value) {
            delay(500) // AI thinking delay
            startTimer()

            // 1. FIRST - Try to win if possible
            val winningMove = findWinningMove(buttons.value, "X")
            if (winningMove != null) {
                buttons.value[winningMove.first][winningMove.second] = "X"
            }
            // 2. SECOND - Block player if they're about to win
            else {
                val blockingMove = findWinningMove(buttons.value, "0")
                if (blockingMove != null) {
                    buttons.value[blockingMove.first][blockingMove.second] = "X"
                }
                // 3. THIRD - Take center if available
                else if(buttons.value[1][1].isEmpty()){
                    buttons.value[1][1] = "X"
                }

                // 4. FOURTH - Take a corner if available
                else {
                    val corners = listOf(0 to 0, 0 to 2, 2 to 0, 2 to 2)
                    val emptyCorners = corners.filter { (row, col) -> buttons.value[row][col].isEmpty()}

                    if (emptyCorners.isNotEmpty()) {
                        val (row, col) = emptyCorners.random()
                        buttons.value[row][col] = "X"
                    }

                    // 5. FIFTH - Take any available edge
                    else {
                        val edges = listOf(0 to 1, 1 to 0, 1 to 2, 2 to 1)
                        val emptyEdges = edges.filter { (row, col) -> buttons.value[row][col].isEmpty() }
                        if (emptyEdges.isNotEmpty()) {
                            val (row, col) = emptyEdges.random()
                            buttons.value[row][col] = "X"
                        }
                    }

                }
            }

            // WIN CHECK
            if (checkWin(buttons.value)) {
                gameOver.value = true
                Toast.makeText(context, "AI wins!", Toast.LENGTH_SHORT).show()
                timer.cancel()
            }
            // Only switch turns if game isn't over
            else {
                currentPlayer.value = "Player 1"
                playerTurn.value = "${currentPlayer.value} Turn"
            }
        }
    }

    /**
     * Saves current game state to history for undo functionality
     */
    fun saveState() {
        history.value = history.value + listOf(buttons.value.map { it.toMutableList() })
    }

    /**
     * Resets the game to initial state
     */
    fun resetGame() {
        buttons.value = List(3) { MutableList(3) { "" } }
        currentPlayer.value = "Player 1"
        playerTurn.value = "${currentPlayer.value} Turn"
        gameOver.value = false
        startTimer()
    }

    /**
     * Undoes the last move (only in PvP mode)
     */
    fun undo() {
        if (isAI.value == true){
            Toast.makeText(context, "Cannot undo against AI!", Toast.LENGTH_SHORT).show()
        }
        if (history.value.isNotEmpty() && isAI.value == false){
            val lastState = history.value.last()
            history.value = history.value.dropLast(1)

            buttons.value = lastState.map { it.toMutableList() }

            // Switch player turn after undo
            currentPlayer.value = if (currentPlayer.value == "Player 1") "Player 2" else "Player 1"
            playerTurn.value = "${currentPlayer.value} Turn"

            gameOver.value = false
        }
        startTimer()
    }

    /**
     * Handles player moves when a button is clicked
     * @param row Row index of clicked button
     * @param col Column index of clicked button
     * @param context Android context for showing Toast messages
     */
    fun onButtonClick(row: Int, col: Int, context: Context) {
        if (gameOver.value) return  // Ignore clicks if game is over


        saveState()     // Save state before making move

        if (isAI.value == false){
            startTimer()    // Only start timer in PvP mode
        }

        // Prevent player from moving when it's AI's turn
        if (isAI.value && currentPlayer.value == "Player 2"){
            return
        }

        // Only make move if the cell is empty
        if (buttons.value[row][col].isEmpty()) {
            // Player 1 uses "0", Player 2/AI uses "X"
            buttons.value[row][col] = if (currentPlayer.value == "Player 1") "0" else "X"
            // Switch player turn
            currentPlayer.value = if (currentPlayer.value == "Player 1") "Player 2" else "Player 1"

            playerTurn.value = "${currentPlayer.value} Turn"

            // Check for win condition
            if (checkWin(buttons.value)) {
                gameOver.value = true
                // Show winner message
                if ("${currentPlayer.value}" == "Player 1") {
                    Toast.makeText(context, "Player 2 is the winner!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Player 1 is the winner!", Toast.LENGTH_SHORT).show()
                }
                timer.cancel()
            }
            // Check for draw if no one won
            else if (checkDraw(buttons.value)) {
                gameOver.value = true
                Toast.makeText(context, "It's a draw!", Toast.LENGTH_SHORT).show()
                timer.cancel()
            }
        }
    }

    // UI Composition
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current

        // Game mode selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // PvP mode button
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

            // AI mode button
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

            // Exit button
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

        // Game title
        Text("Tic Tac Toe", color = Color.Blue, style = TextStyle(fontSize = 30.sp))

        Spacer(modifier = Modifier.height(10.dp))

        // Timer display with color change when time is low
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("Time Left: ")
                }
                withStyle(style = SpanStyle(color = if (timeLeft < 10) Color.Red else Color.Black)) {
                    append("$timeLeft")
                }
            },
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current player turn display
        Text(playerTurn.value, style = TextStyle(fontSize = 24.sp))

        Spacer(modifier = Modifier.height(23.dp))

        // Game board
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Create 3x3 grid of buttons
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
                                // Display "0" or "X" based on cell state
                                when (buttons.value[rowIndex][colIndex]) {
                                    "0" -> DrawCircle(color = Color.Blue)
                                    "X" -> DrawCross(color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Game control buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Reset game button
            Button(
                onClick = { resetGame() },
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
            ) {
                Text("↻", color = Color.Black, style = TextStyle(fontSize = 30.sp))
            }

            // Undo move button
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

/**
 * Main activity that hosts the game
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock screen orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE0F7FA)
                ) {
                    TicTacBoard()
                }
            }
        }
    }
}