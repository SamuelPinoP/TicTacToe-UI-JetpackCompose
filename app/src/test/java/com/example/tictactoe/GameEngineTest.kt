package com.example.tictactoe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    @Test
    fun evaluateDetectsWinningLine() {
        val board = boardOf(
            Mark.X, Mark.X, Mark.X,
            Mark.Empty, Mark.O, Mark.Empty,
            Mark.O, Mark.Empty, Mark.Empty
        )

        val outcome = TicTacToeEngine.evaluate(board)

        assertTrue(outcome is GameOutcome.Win)
        outcome as GameOutcome.Win
        assertEquals(Mark.X, outcome.winner)
        assertEquals(
            listOf(Move(0, 0), Move(0, 1), Move(0, 2)),
            outcome.line.cells
        )
    }

    @Test
    fun makeMoveRecordsScoreWhenRoundEnds() {
        var state = GameState(mode = GameMode.Local)

        state = TicTacToeEngine.makeMove(state, Move(0, 0))
        state = TicTacToeEngine.makeMove(state, Move(1, 0))
        state = TicTacToeEngine.makeMove(state, Move(0, 1))
        state = TicTacToeEngine.makeMove(state, Move(1, 1))
        state = TicTacToeEngine.makeMove(state, Move(0, 2))

        assertTrue(state.outcome is GameOutcome.Win)
        assertEquals(Mark.X, state.outcome.winner)
        assertEquals(1, state.score.xWins)
        assertEquals(0, state.score.oWins)
        assertEquals(0, state.score.draws)
    }

    @Test
    fun tacticalAiBlocksImmediateThreat() {
        val board = boardOf(
            Mark.X, Mark.X, Mark.Empty,
            Mark.O, Mark.Empty, Mark.Empty,
            Mark.Empty, Mark.Empty, Mark.Empty
        )

        val move = TicTacToeEngine.bestMove(
            board = board,
            mark = Mark.O,
            difficulty = AiDifficulty.Tactical
        )

        assertEquals(Move(0, 2), move)
    }

    @Test
    fun expertAiTakesImmediateWin() {
        val board = boardOf(
            Mark.O, Mark.O, Mark.Empty,
            Mark.X, Mark.X, Mark.Empty,
            Mark.Empty, Mark.Empty, Mark.Empty
        )

        val move = TicTacToeEngine.bestMove(
            board = board,
            mark = Mark.O,
            difficulty = AiDifficulty.Expert
        )

        assertEquals(Move(0, 2), move)
    }

    @Test
    fun expertAiAvoidsKnownCornerFork() {
        val board = boardOf(
            Mark.X, Mark.Empty, Mark.Empty,
            Mark.Empty, Mark.O, Mark.Empty,
            Mark.Empty, Mark.Empty, Mark.X
        )

        val move = TicTacToeEngine.bestMove(
            board = board,
            mark = Mark.O,
            difficulty = AiDifficulty.Expert
        )

        assertTrue(
            move in setOf(Move(0, 1), Move(1, 0), Move(1, 2), Move(2, 1))
        )
    }

    @Test
    fun undoAgainstComputerRemovesHumanMoveAndComputerReply() {
        var state = GameState(mode = GameMode.Computer, difficulty = AiDifficulty.Tactical)

        state = TicTacToeEngine.makeMove(state, Move(0, 0))
        state = TicTacToeEngine.playComputerTurn(state)
        val undone = TicTacToeEngine.undo(state)

        assertEquals(TicTacToeEngine.emptyBoard(), undone.board)
        assertEquals(Mark.X, undone.currentTurn)
        assertEquals(GameOutcome.InProgress, undone.outcome)
        assertTrue(undone.history.isEmpty())
    }

    private fun boardOf(vararg cells: Mark): List<Mark> = cells.toList()
}
