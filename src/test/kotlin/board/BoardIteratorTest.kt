package board

import model.board.Board
import org.junit.Test
import kotlin.test.assertEquals

internal class BoardIteratorTest {

    @Test
    fun iterateOverBoard() {
        val blockSize = 4
        val board = Board(blockSize)

        val expectedValues = ArrayList<Pair<Int, Int>>(blockSize*blockSize)
        for (row in 0 until board.size) {
            for (col in 0 until blockSize*blockSize) {
                expectedValues.add(Pair(row, col))
            }
        }

        val actualValues = ArrayList<Pair<Int, Int>>(blockSize*blockSize)
        for (cell in board) {
            val (row, col) = cell
            actualValues.add(Pair(row,col))
        }

        assertEquals(expectedValues, actualValues)
    }
}