package com.example.tictactoe

const val TurnDurationSeconds = 20

enum class Mark(val symbol: String) {
    Empty(""),
    X("X"),
    O("O");

    fun opponent(): Mark = when (this) {
        X -> O
        O -> X
        Empty -> Empty
    }
}

enum class GameMode(val label: String) {
    Local("Two Player"),
    Computer("Computer")
}

enum class AiDifficulty(val label: String) {
    Practice("Practice"),
    Tactical("Tactical"),
    Expert("Expert")
}

data class Move(val row: Int, val col: Int) {
    init {
        require(row in 0..2) { "row must be between 0 and 2" }
        require(col in 0..2) { "col must be between 0 and 2" }
    }

    val index: Int = row * 3 + col
}

data class WinningLine(val cells: List<Move>)

sealed class GameOutcome {
    object InProgress : GameOutcome()
    object Draw : GameOutcome()
    data class Win(val winner: Mark, val line: WinningLine) : GameOutcome()
    data class Forfeit(val winner: Mark) : GameOutcome()
}

val GameOutcome.isFinished: Boolean
    get() = this !is GameOutcome.InProgress

val GameOutcome.winner: Mark?
    get() = when (this) {
        is GameOutcome.Win -> winner
        is GameOutcome.Forfeit -> winner
        GameOutcome.Draw,
        GameOutcome.InProgress -> null
    }

data class Score(
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0
) {
    fun record(outcome: GameOutcome): Score = when (outcome) {
        is GameOutcome.Win -> recordWin(outcome.winner)
        is GameOutcome.Forfeit -> recordWin(outcome.winner)
        GameOutcome.Draw -> copy(draws = draws + 1)
        GameOutcome.InProgress -> this
    }

    private fun recordWin(winner: Mark): Score = when (winner) {
        Mark.X -> copy(xWins = xWins + 1)
        Mark.O -> copy(oWins = oWins + 1)
        Mark.Empty -> this
    }
}

data class GameSnapshot(
    val board: List<Mark>,
    val currentTurn: Mark,
    val outcome: GameOutcome,
    val score: Score
)

data class GameState(
    val board: List<Mark> = TicTacToeEngine.emptyBoard(),
    val currentTurn: Mark = Mark.X,
    val outcome: GameOutcome = GameOutcome.InProgress,
    val mode: GameMode = GameMode.Computer,
    val difficulty: AiDifficulty = AiDifficulty.Expert,
    val score: Score = Score(),
    val history: List<GameSnapshot> = emptyList()
) {
    val isComputerTurn: Boolean
        get() = mode == GameMode.Computer &&
            currentTurn == TicTacToeEngine.ComputerMark &&
            outcome == GameOutcome.InProgress
}

object TicTacToeEngine {
    val HumanMark = Mark.X
    val ComputerMark = Mark.O

    private val winTriples = listOf(
        listOf(Move(0, 0), Move(0, 1), Move(0, 2)),
        listOf(Move(1, 0), Move(1, 1), Move(1, 2)),
        listOf(Move(2, 0), Move(2, 1), Move(2, 2)),
        listOf(Move(0, 0), Move(1, 0), Move(2, 0)),
        listOf(Move(0, 1), Move(1, 1), Move(2, 1)),
        listOf(Move(0, 2), Move(1, 2), Move(2, 2)),
        listOf(Move(0, 0), Move(1, 1), Move(2, 2)),
        listOf(Move(0, 2), Move(1, 1), Move(2, 0))
    )

    private val moveOrder = listOf(
        Move(1, 1),
        Move(0, 0),
        Move(0, 2),
        Move(2, 0),
        Move(2, 2),
        Move(0, 1),
        Move(1, 0),
        Move(1, 2),
        Move(2, 1)
    )

    fun emptyBoard(): List<Mark> = List(9) { Mark.Empty }

    fun newRound(state: GameState): GameState = state.copy(
        board = emptyBoard(),
        currentTurn = Mark.X,
        outcome = GameOutcome.InProgress,
        history = emptyList()
    )

    fun resetMatch(state: GameState): GameState = GameState(
        mode = state.mode,
        difficulty = state.difficulty
    )

    fun setMode(state: GameState, mode: GameMode): GameState = resetMatch(
        state.copy(mode = mode)
    )

    fun setDifficulty(state: GameState, difficulty: AiDifficulty): GameState = newRound(
        state.copy(difficulty = difficulty)
    )

    fun makeMove(state: GameState, move: Move): GameState {
        if (state.outcome.isFinished || state.board[move.index] != Mark.Empty) {
            return state
        }

        val nextBoard = state.board.place(move, state.currentTurn)
        val nextOutcome = evaluate(nextBoard)
        val nextTurn = if (nextOutcome == GameOutcome.InProgress) {
            state.currentTurn.opponent()
        } else {
            state.currentTurn
        }

        return state.copy(
            board = nextBoard,
            currentTurn = nextTurn,
            outcome = nextOutcome,
            score = if (nextOutcome.isFinished) state.score.record(nextOutcome) else state.score,
            history = state.history + state.snapshot()
        )
    }

    fun playComputerTurn(state: GameState): GameState {
        if (!state.isComputerTurn) {
            return state
        }

        val move = bestMove(
            board = state.board,
            mark = ComputerMark,
            difficulty = state.difficulty
        ) ?: return state

        return makeMove(state, move)
    }

    fun undo(state: GameState): GameState {
        if (state.history.isEmpty()) {
            return state
        }

        val steps = undoSteps(state)
        val target = state.history[state.history.size - steps]

        return state.copy(
            board = target.board,
            currentTurn = target.currentTurn,
            outcome = target.outcome,
            score = target.score,
            history = state.history.dropLast(steps)
        )
    }

    fun forfeitCurrentTurn(state: GameState): GameState {
        if (state.outcome.isFinished) {
            return state
        }

        val outcome = GameOutcome.Forfeit(state.currentTurn.opponent())
        return state.copy(
            outcome = outcome,
            score = state.score.record(outcome),
            history = state.history + state.snapshot()
        )
    }

    fun evaluate(board: List<Mark>): GameOutcome {
        require(board.size == 9) { "Tic Tac Toe board must have exactly 9 cells" }

        winTriples.forEach { line ->
            val marks = line.map { board[it.index] }
            val first = marks.first()
            if (first != Mark.Empty && marks.all { it == first }) {
                return GameOutcome.Win(first, WinningLine(line))
            }
        }

        return if (board.none { it == Mark.Empty }) {
            GameOutcome.Draw
        } else {
            GameOutcome.InProgress
        }
    }

    fun availableMoves(board: List<Mark>): List<Move> {
        require(board.size == 9) { "Tic Tac Toe board must have exactly 9 cells" }
        return moveOrder.filter { board[it.index] == Mark.Empty }
    }

    fun bestMove(
        board: List<Mark>,
        mark: Mark,
        difficulty: AiDifficulty
    ): Move? = when (difficulty) {
        AiDifficulty.Practice -> practiceMove(board)
        AiDifficulty.Tactical -> tacticalMove(board, mark)
        AiDifficulty.Expert -> expertMove(board, mark)
    }

    private fun practiceMove(board: List<Mark>): Move? = availableMoves(board).firstOrNull()

    private fun tacticalMove(board: List<Mark>, mark: Mark): Move? =
        immediateWinningMove(board, mark)
            ?: immediateWinningMove(board, mark.opponent())
            ?: availableMoves(board).firstOrNull()

    private fun expertMove(board: List<Mark>, mark: Mark): Move? =
        availableMoves(board)
            .map { move ->
                val nextBoard = board.place(move, mark)
                ScoredMove(
                    move = move,
                    score = minimax(
                        board = nextBoard,
                        currentTurn = mark.opponent(),
                        maximizingMark = mark,
                        depth = 1,
                        alpha = Int.MIN_VALUE,
                        beta = Int.MAX_VALUE
                    )
                )
            }
            .maxWithOrNull(compareBy<ScoredMove> { it.score }.thenBy { it.move.desirability })
            ?.move

    private fun minimax(
        board: List<Mark>,
        currentTurn: Mark,
        maximizingMark: Mark,
        depth: Int,
        alpha: Int,
        beta: Int
    ): Int {
        when (val outcome = evaluate(board)) {
            is GameOutcome.Win -> return if (outcome.winner == maximizingMark) {
                10 - depth
            } else {
                depth - 10
            }

            GameOutcome.Draw -> return 0
            is GameOutcome.Forfeit -> return 0
            GameOutcome.InProgress -> Unit
        }

        val moves = availableMoves(board)
        var localAlpha = alpha
        var localBeta = beta

        return if (currentTurn == maximizingMark) {
            var bestScore = Int.MIN_VALUE
            for (move in moves) {
                bestScore = maxOf(
                    bestScore,
                    minimax(
                        board = board.place(move, currentTurn),
                        currentTurn = currentTurn.opponent(),
                        maximizingMark = maximizingMark,
                        depth = depth + 1,
                        alpha = localAlpha,
                        beta = localBeta
                    )
                )
                localAlpha = maxOf(localAlpha, bestScore)
                if (localBeta <= localAlpha) break
            }
            bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (move in moves) {
                bestScore = minOf(
                    bestScore,
                    minimax(
                        board = board.place(move, currentTurn),
                        currentTurn = currentTurn.opponent(),
                        maximizingMark = maximizingMark,
                        depth = depth + 1,
                        alpha = localAlpha,
                        beta = localBeta
                    )
                )
                localBeta = minOf(localBeta, bestScore)
                if (localBeta <= localAlpha) break
            }
            bestScore
        }
    }

    private fun immediateWinningMove(board: List<Mark>, mark: Mark): Move? =
        availableMoves(board).firstOrNull { move ->
            evaluate(board.place(move, mark)) is GameOutcome.Win
        }

    private fun undoSteps(state: GameState): Int {
        if (state.mode != GameMode.Computer || state.history.size < 2) {
            return 1
        }

        val outcome = state.outcome
        val shouldUndoComputerReply = when {
            outcome is GameOutcome.Win && outcome.winner == ComputerMark -> true
            outcome == GameOutcome.Draw -> true
            outcome == GameOutcome.InProgress && state.currentTurn == HumanMark -> true
            else -> false
        }

        return if (shouldUndoComputerReply) 2 else 1
    }

    private fun GameState.snapshot(): GameSnapshot = GameSnapshot(
        board = board,
        currentTurn = currentTurn,
        outcome = outcome,
        score = score
    )

    private fun List<Mark>.place(move: Move, mark: Mark): List<Mark> =
        toMutableList().also { cells -> cells[move.index] = mark }

    private val Move.desirability: Int
        get() = when (this) {
            Move(1, 1) -> 4
            Move(0, 0),
            Move(0, 2),
            Move(2, 0),
            Move(2, 2) -> 3
            else -> 2
        }

    private data class ScoredMove(
        val move: Move,
        val score: Int
    )
}
