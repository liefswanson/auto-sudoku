package board

import model.board.Board
import org.junit.Test
import textUI.LARGEST_PARSABLE_BOARD
import textUI.SMALLEST_PARSABLE_BOARD
import java.lang.Math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BoardTest {

    @Test
    fun getTest() {

        val board = Board()
        val expectedOptions = (1..board.size).toSet()

        for (row in 0 until board.size) {
            for (col in 0 until board.size) {

                val (actualRow, actualCol) = board[row, col]
                val actualOptions = board[row, col].possibilities
                assertEquals(row, actualRow)
                assertEquals(col, actualCol)

                assertEquals(expectedOptions, actualOptions)
            }
        }
    }

    @Test
    fun constructionTest() {
        constructionTestHelper(LARGEST_PARSABLE_BOARD)
        constructionTestHelper(Board.DEFAULT_SIZE)
        constructionTestHelper(SMALLEST_PARSABLE_BOARD)
    }

    private fun constructionTestHelper(blockSize: Int) {
        val board = Board(blockSize)

        for (cell in board) {
            assertEquals(board.size, cell.rowCells.toSet().size)
            assertTrue(cell.rowCells.all { it.rowCells == cell.rowCells })
            assertTrue(cell.rowCells.all { it.row == cell.row })

            assertEquals(board.size, cell.colCells.toSet().size)
            assertTrue(cell.colCells.all { it.colCells == cell.colCells })
            assertTrue(cell.colCells.all { it.col == cell.col })

            assertEquals(board.size, cell.blockCells.toSet().size)
            assertTrue(cell.blockCells.all { it.blockCells == cell.blockCells })
            assertTrue(cell.blockCells.all {
                val validRange = 0 until board.size
                val rowOffset = abs(it.row - cell.row)
                val colOffset = abs(it.col - cell.col)

                rowOffset in validRange && colOffset in validRange
            })
        }
    }

    @Test
    fun toStringTest1() {
        val row =  "1\n"
        toStringTestHelper(row, "", 1)
    }

    // these are all done partly by hand instead of entirely through algorithmic construction
    // because i found it easier to write the tests correctly by hand
    @Test
    fun toStringTest2() {

        val row = "**│**\n"
        val div = "──┼──\n"
        toStringTestHelper(row, div, 2)
    }

    @Test
    fun toStringTest3() {
        val row = "***│***│***\n"
        val div = "───┼───┼───\n"
        toStringTestHelper(row, div, 3)
    }

    @Test
    fun toStringTest4() {

        val row = "****│****│****│****\n"
        val div = "────┼────┼────┼────\n"
        toStringTestHelper(row, div, 4)
    }

    private fun toStringTestHelper(row: String, divider: String, blockSize: Int) {
        val blocks =  row.repeat(blockSize)

        val expect = (blocks + divider).repeat(blockSize-1) + blocks

        val actual = Board(blockSize).toString()

        assertEquals(expect, actual)
    }
}