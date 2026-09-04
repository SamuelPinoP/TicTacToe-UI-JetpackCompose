package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tictactoe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.delay

@Composable
fun TicTacBoard() {
    var state by remember { mutableStateOf(GameState()) }
    var timeLeft by remember { mutableStateOf(TurnDurationSeconds) }

    LaunchedEffect(state.board, state.currentTurn, state.outcome) {
        timeLeft = TurnDurationSeconds
        if (state.outcome != GameOutcome.InProgress) return@LaunchedEffect

        while (timeLeft > 0 && state.outcome == GameOutcome.InProgress) {
            delay(1_000)
            timeLeft -= 1
        }

        if (timeLeft == 0 && state.outcome == GameOutcome.InProgress) {
            state = TicTacToeEngine.forfeitCurrentTurn(state)
        }
    }

    LaunchedEffect(
        state.board,
        state.currentTurn,
        state.mode,
        state.difficulty,
        state.outcome
    ) {
        if (state.isComputerTurn) {
            delay(450)
            state = TicTacToeEngine.playComputerTurn(state)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            GameHeader(state = state, timeLeft = timeLeft)
            MatchControls(
                state = state,
                onModeSelected = { mode ->
                    state = TicTacToeEngine.setMode(state, mode)
                },
                onDifficultySelected = { difficulty ->
                    state = TicTacToeEngine.setDifficulty(state, difficulty)
                }
            )
            ScoreBoard(state = state)
            StatusPanel(state = state)
            GameBoard(
                state = state,
                onCellSelected = { move ->
                    if (!state.isComputerTurn) {
                        state = TicTacToeEngine.makeMove(state, move)
                    }
                }
            )
            GameActions(
                canUndo = state.history.isNotEmpty(),
                onNewRound = {
                    state = TicTacToeEngine.newRound(state)
                },
                onUndo = {
                    state = TicTacToeEngine.undo(state)
                },
                onResetMatch = {
                    state = TicTacToeEngine.resetMatch(state)
                }
            )
        }
    }
}

@Composable
private fun GameHeader(state: GameState, timeLeft: Int) {
    val progress = timeLeft / TurnDurationSeconds.toFloat()
    val timerColor = if (timeLeft <= 5) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Tic Tac Toe Arena",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = headerSubtitle(state),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Turn timer",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "${timeLeft}s",
                color = timerColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun MatchControls(
    state: GameState,
    onModeSelected: (GameMode) -> Unit,
    onDifficultySelected: (AiDifficulty) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChoiceRow(
            title = "Mode",
            choices = GameMode.values().map { mode ->
                Choice(
                    label = mode.label,
                    selected = state.mode == mode,
                    onClick = { onModeSelected(mode) }
                )
            }
        )

        if (state.mode == GameMode.Computer) {
            ChoiceRow(
                title = "AI",
                choices = AiDifficulty.values().map { difficulty ->
                    Choice(
                        label = difficulty.label,
                        selected = state.difficulty == difficulty,
                        onClick = { onDifficultySelected(difficulty) }
                    )
                }
            )
        }
    }
}

@Composable
private fun ChoiceRow(title: String, choices: List<Choice>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            choices.forEach { choice ->
                ChoiceButton(
                    choice = choice,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChoiceButton(choice: Choice, modifier: Modifier = Modifier) {
    val containerColor = if (choice.selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (choice.selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    OutlinedButton(
        onClick = choice.onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (choice.selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = choice.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScoreBoard(state: GameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScoreTile(
            title = Mark.X.scoreLabel(state),
            value = state.score.xWins,
            accent = Color(0xFFE84A5F),
            modifier = Modifier.weight(1f)
        )
        ScoreTile(
            title = "Draws",
            value = state.score.draws,
            accent = Color(0xFFFFC857),
            modifier = Modifier.weight(1f)
        )
        ScoreTile(
            title = Mark.O.scoreLabel(state),
            value = state.score.oWins,
            accent = Color(0xFF1D8A99),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreTile(
    title: String,
    value: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = accent,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusPanel(state: GameState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        shape = RoundedCornerShape(8.dp),
        color = statusColor(state),
        contentColor = statusContentColor(state),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = statusTitle(state),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = statusDetail(state),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GameBoard(
    state: GameState,
    onCellSelected: (Move) -> Unit
) {
    val winningCells = when (val outcome = state.outcome) {
        is GameOutcome.Win -> outcome.line.cells.map { it.index }.toSet()
        GameOutcome.Draw,
        is GameOutcome.Forfeit,
        GameOutcome.InProgress -> emptySet()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(
                    color = Color(0xFF102027),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0..2) {
                        val move = Move(row, col)
                        BoardCell(
                            mark = state.board[move.index],
                            move = move,
                            isHighlighted = move.index in winningCells,
                            enabled = state.outcome == GameOutcome.InProgress &&
                                !state.isComputerTurn &&
                                state.board[move.index] == Mark.Empty,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                onCellSelected(move)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    mark: Mark,
    move: Move,
    isHighlighted: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val backgroundColor = when {
        isHighlighted -> Color(0xFFFFF0B8)
        mark == Mark.Empty -> MaterialTheme.colorScheme.surface
        else -> Color(0xFFF6FAFA)
    }
    val borderColor = if (isHighlighted) {
        Color(0xFFFFC857)
    } else {
        Color(0xFF25424A)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(2.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                contentDescription = cellDescription(mark, move)
            },
        contentAlignment = Alignment.Center
    ) {
        DrawMark(mark = mark, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun GameActions(
    canUndo: Boolean,
    onNewRound: () -> Unit,
    onUndo: () -> Unit,
    onResetMatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNewRound,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("New round", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Undo", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        OutlinedButton(
            onClick = onResetMatch,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Reset match", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

private data class Choice(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

private fun headerSubtitle(state: GameState): String = when (state.mode) {
    GameMode.Local -> "Local match. X opens each round."
    GameMode.Computer -> "You are X. ${state.difficulty.label} AI plays O."
}

private fun statusTitle(state: GameState): String = when (val outcome = state.outcome) {
    GameOutcome.InProgress -> if (state.isComputerTurn) {
        "Computer thinking"
    } else {
        "${state.currentTurn.playerLabel(state)} to move"
    }

    GameOutcome.Draw -> "Round draw"
    is GameOutcome.Forfeit -> "${outcome.winner.playerLabel(state)} wins on time"
    is GameOutcome.Win -> "${outcome.winner.playerLabel(state)} wins"
}

private fun statusDetail(state: GameState): String = when (val outcome = state.outcome) {
    GameOutcome.InProgress -> when (state.mode) {
        GameMode.Local -> "Current mark: ${state.currentTurn.symbol}"
        GameMode.Computer -> if (state.isComputerTurn) {
            "AI mark: ${TicTacToeEngine.ComputerMark.symbol}"
        } else {
            "Your mark: ${TicTacToeEngine.HumanMark.symbol}"
        }
    }

    GameOutcome.Draw -> "No three-in-a-row remained open."
    is GameOutcome.Forfeit -> "The turn timer reached zero."
    is GameOutcome.Win -> "Winning line: ${outcome.line.cells.joinToString { "r${it.row + 1}c${it.col + 1}" }}"
}

@Composable
private fun statusColor(state: GameState): Color = when (state.outcome) {
    GameOutcome.InProgress -> MaterialTheme.colorScheme.primaryContainer
    GameOutcome.Draw -> MaterialTheme.colorScheme.tertiaryContainer
    is GameOutcome.Forfeit,
    is GameOutcome.Win -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
private fun statusContentColor(state: GameState): Color = when (state.outcome) {
    GameOutcome.InProgress -> MaterialTheme.colorScheme.onPrimaryContainer
    GameOutcome.Draw -> MaterialTheme.colorScheme.onTertiaryContainer
    is GameOutcome.Forfeit,
    is GameOutcome.Win -> MaterialTheme.colorScheme.onSecondaryContainer
}

private fun Mark.playerLabel(state: GameState): String = when (state.mode) {
    GameMode.Local -> "Player $symbol"
    GameMode.Computer -> when (this) {
        Mark.X -> "You"
        Mark.O -> "Computer"
        Mark.Empty -> "Open"
    }
}

private fun Mark.scoreLabel(state: GameState): String = when (state.mode) {
    GameMode.Local -> "Player $symbol"
    GameMode.Computer -> when (this) {
        Mark.X -> "You"
        Mark.O -> "AI"
        Mark.Empty -> "Open"
    }
}

private fun cellDescription(mark: Mark, move: Move): String {
    val cell = "Row ${move.row + 1}, column ${move.col + 1}"
    return if (mark == Mark.Empty) {
        "$cell, empty"
    } else {
        "$cell, ${mark.symbol}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicTacToeTheme(dynamicColor = false) {
                TicTacBoard()
            }
        }
    }
}
