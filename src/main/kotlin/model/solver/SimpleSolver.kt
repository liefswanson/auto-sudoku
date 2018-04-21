package model.solver

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import model.board.Board
import model.board.BoardPartition
import model.board.Cell

object SimpleSolver : Solver{
    override fun attemptSolveOf(board: Board): Boolean{

        val rows = board.rows
        val cols = board.cols
        val blocks = board.blocks

        val nakedSingles =
                rows.solveAllNakedSingles() or
                cols.solveAllNakedSingles() or
                blocks.solveAllNakedSingles()

        val hiddenSingles =
                rows.solveAllHiddenSingles() or
                cols.solveAllHiddenSingles() or
                blocks.solveAllHiddenSingles()

        return nakedSingles || hiddenSingles
    }

    // TODO fix this broken garbage
    @Suppress("UNUSED")
    private fun performIntersection(block: BoardPartition, line: BoardPartition): Boolean {
        var modified = false
        for (i in (1 .. line.size)) {
            val cells = block.filter { i in it }

            if (cells.all { it !in line }) {
                continue
            }

            val targetCells = line.filter { it !in block }
            assert (targetCells.size < line.size, {"Nothing was filtered..."})


            val removed = targetCells.evalAndAccumulateWith({ it.remove(i) }, Boolean::or)
            modified = removed or modified
        }

        return modified
    }

    override fun getStateOf(board: Board):SolveState {

        val parts = listOf(
                board.rows,
                board.cols,
                board.blocks
        ).flatten()

        val contradiction =
                board.any { it.isImpossible() } ||
                parts.findContradictions()

        return when {
            contradiction -> SolveState.Contradiction

            board.all{ it.isSolved() } -> SolveState.Done

            else -> SolveState.InProgress
        }
    }

    private fun BoardPartition.containsContradiction(): Boolean {
        val toCheck = this
                .filter { it.isSolved() }
                .map { it.onlyPossibility() }

        return toCheck.size > toCheck.toSet().size
    }

    private fun Iterable<BoardPartition>.solveAllHiddenSingles():Boolean =
            evalInThreadPool({ it.solveHiddenSingles() }, Boolean::or)

    private fun Iterable<BoardPartition>.solveAllNakedSingles():Boolean =
            evalInThreadPool({ it.solveNakedSingles() }, Boolean::or)

    private fun Iterable<BoardPartition>.findContradictions():Boolean =
            evalInThreadPool({ it.containsContradiction() }, Boolean::or)

    private fun<T> Iterable<T>.evalInThreadPool(
            method: (T) -> Boolean,
            accumulator: Boolean.(Boolean) -> Boolean
    ): Boolean {

        val results = run {
            this.map {
                async { method(it) }
            }
        }

        return runBlocking {
            results.evalAndAccumulateWith({ it.await() }, accumulator)
        }
    }

    private fun BoardPartition.solveNakedSingles():Boolean {

        val (solved, unsolved) = this.partition { it.isSolved() }
        val toEliminate = solved.map { it.onlyPossibility()!! } // solved, so this won't be null

        return unsolved.evalAndAccumulateWith({ it.removeAll(toEliminate) }, Boolean::or)
    }


    private fun BoardPartition.solveHiddenSingles():Boolean {
        val keys = (1 .. this.size)
        val values = keys.map{ ArrayList<Cell>().toMutableList() }
        val reverseMap = keys.zip(values).toMap()

        for (cell in this) {
            for (possibility in cell) {
                reverseMap[possibility]!!.add(cell) // all values are in the map by definition
            }
        }

        val toEliminate = reverseMap
                .filter { (_, cells) -> cells.size == 1 } // the only cell that has this possibility
                .map { (i, cells) -> i to cells.first() }
                .filter { (_, cell) -> cell.isNotSolved()}

        for ((i, cell) in toEliminate) {
            cell.copyChoices(i)
        }

        return toEliminate.isNotEmpty()
    }

    private inline fun<T> Iterable<T>.evalAndAccumulateWith(predicate: (T) -> Boolean,
                                                            accumulator: Boolean.(Boolean) -> Boolean): Boolean =
            this.fold(false) { acc, elem -> accumulator( predicate(elem), acc) }
}