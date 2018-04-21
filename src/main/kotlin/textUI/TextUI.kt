package textUI

import model.board.Board
import model.solver.SolveState
import model.solver.Solver

class TextUI(private val solver: Solver,
             private var board: Board
) {

    fun executeCommand(): Command {
        val command = getCommand()

        when(command) {
            //Command.Set -> setCell()
            Command.Solve -> {
                board = solve(board)
                if (solver.getStateOf(board) != SolveState.Done) {
                    println("The board was not solvable, sorry! Check input file for mistakes.")
                }
            }
            Command.Help ->
                for (option in Command.values()) {
                    println(option.description)
                }
            Command.Show -> println(board)
            Command.Quit -> println("quitting...")
            //else -> println("$command unimplemented")
        }

        return command
    }

    private fun getCommand(): Command {
        val input = readInputFromUser("please input a command")
        val command = mapInputToCommand(input)

        if (command == null) {
            println("invalid command: $input")
            return getCommand()
        }

        return command
    }

    // might be argued this should be in the solver... but as it needs to output, it was easier to put here
    private fun solve(board: Board): Board {
        var state = SolveState.InProgress
        var modified = true

        while(state == SolveState.InProgress && modified) {
            modified = solver.attemptSolveOf(board)
            state = solver.getStateOf(board)

            println(board)
        }


        when(state) {
            SolveState.Contradiction -> println("Contradiction found, reverting to last valid state... ")
            SolveState.Done          -> println("Done!")
            SolveState.InProgress    -> return recursiveSolve(board) // ***IMPORTANT*** mutual recursion happens here!
        }

        return board
    }

    private fun recursiveSolve(board: Board):Board {
        assert(solver.getStateOf(board) == SolveState.InProgress,
                {"should only be called on partially solved boards"})

        val guessTarget = board.cells
                .filter { it.isNotSolved() }
                .minBy { it.possibilities.size }!! // only null when board is solved, which it isn't

        val hash = board.hashCode()
        val positive = hash.toUnsignedLong()
        val hex = String.format("%08x", positive)

        val (row, col) = guessTarget
        for (possibility in guessTarget) {
            val copy = board.copy()
            val copyCell = copy[row, col]

            println("making a guess on board $hex... (${row+1}, ${col+1}) = $possibility")
            println()
            copyCell.copyChoices(possibility)

            val result = solve(copy) // ***IMPORTANT*** mutual recursion happens here!

            val state = solver.getStateOf(result)
            if (state == SolveState.Done) return result
            assert(state != SolveState.InProgress,
                    {"Should not be possible, state should always be Done or Contradiction"})
            // if contradiction, just try a different guess
        }

        // happens when every guess resulted in contradiction
        println("discarding board $hex...")
        return board
    }

    private fun Int.toUnsignedLong(): Long = if (this >= 0) toLong() else twosCompliment()
    private fun Int.twosCompliment(): Long = inv().toLong() + 1

}