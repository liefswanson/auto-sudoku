package model.solver

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import model.board.Board
import model.board.BoardPartition
import model.board.Cell

object ThreadedSimpleSolver : Solver{
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
            val cells = block.filter { cell -> i in cell }

            if (cells.all { cell -> cell !in line }) {
                continue
            }

            val targetCells = line.filter { it !in block }
            assert (targetCells.size < line.size, {"Nothing was filtered..."})


            val removed = targetCells.evalThenOr { it.remove(i) }
            modified = removed or modified
        }

        return modified
    }

    override fun getStateOf(board: Board):SolveState {

        val partitions = listOf(
                board.rows,
                board.cols,
                board.blocks
        ).flatten()

        val contradiction =
                board.any { it.isImpossible() } ||
                partitions.findContradictions()

        return when {
            contradiction -> SolveState.Contradiction

            board.rows.isFullySolved() -> SolveState.Done

            else -> SolveState.InProgress
        }
    }

    private fun Iterable<BoardPartition>.isFullySolved(): Boolean =
            threadPooledAnd { part -> part.isFullySolved() }
    private fun BoardPartition.isFullySolved(): Boolean =
            all { cell -> cell.isSolved() }

    private fun BoardPartition.containsContradiction(): Boolean {
        val toCheck = this
                .filter { cell -> cell.isSolved() }
                .map { cell -> cell.onlyPossibility() }

        return toCheck.size > toCheck.toSet().size
    }

    private fun Iterable<BoardPartition>.solveAllHiddenSingles():Boolean =
            threadPooledOr{ part -> part.solveHiddenSingles() }

    private fun Iterable<BoardPartition>.solveAllNakedSingles():Boolean =
            threadPooledOr{ part -> part.solveNakedSingles() }

    private fun Iterable<BoardPartition>.findContradictions():Boolean =
            threadPooledOr{ part -> part.containsContradiction() }

    private fun BoardPartition.solveNakedSingles():Boolean {

        val (solved, unsolved) = this.partition { cell -> cell.isSolved() }
        val toEliminate = solved.map { cell -> cell.onlyPossibility()!! } // solved, so this won't be null

        return unsolved.evalThenOr{ cell -> cell.removeAll(toEliminate) }
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

    // TODO fix code duplication from requiring inline functions for await()
    private fun<T> Iterable<T>.threadPooledOr(
            predicate: (T) -> Boolean
    ): Boolean {

        val results = run {
            this.map {
                async { predicate(it) }
            }
        }

        return runBlocking {
            results.evalThenOr{ result -> result.await() }
        }
    }

    private fun<T> Iterable<T>.threadPooledAnd(
            predicate: (T) -> Boolean
    ): Boolean {

        val results = run {
            this.map {
                async { predicate(it) }
            }
        }

        return runBlocking {
            results.evalThenAnd{ result -> result.await() }
        }

    }

    private inline fun<T> Iterable<T>.evalThenOr(predicate: (T) -> Boolean): Boolean =
            fold(false) { acc, elem -> predicate(elem) or  acc }
    private inline fun<T> Iterable<T>.evalThenAnd(predicate: (T) -> Boolean): Boolean =
            fold(true)  { acc, elem -> predicate(elem) and acc }
}