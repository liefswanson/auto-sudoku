package model.solver

import model.board.Board

interface Solver {
    fun attemptSolveOf(board: Board): Boolean
    fun getStateOf(board: Board): SolveState
}