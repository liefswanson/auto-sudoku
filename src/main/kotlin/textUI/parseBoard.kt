package textUI

import model.board.Board
import model.board.Cell
import model.board.InvalidBoardException
import utils.multicatch

const val SMALLEST_PARSABLE_BOARD = 1
const val LARGEST_PARSABLE_BOARD = 6
const val LARGEST_BASE = LARGEST_PARSABLE_BOARD * LARGEST_PARSABLE_BOARD

private val VALID_CHARS = listOf(Cell.EMPTY) + ('1'..'9') + ('a'..'z') + '0'
private val VALID_CHARMAP = VALID_CHARS.zip(0 until VALID_CHARS.size).toMap()

fun parseBoard(input: String): Board {
    val lines = input.split("\n")
            .filter { it.isNotEmpty() }

    val blockSize = try { lines[0].trim().toInt() }
    catch (e: Exception) {
        e.multicatch(
                NumberFormatException::class,
                IndexOutOfBoundsException::class) {

            throw InvalidBoardException("first line of file must be a block size between " +
                    "$SMALLEST_PARSABLE_BOARD & $LARGEST_PARSABLE_BOARD")
        }
    }

    if (blockSize !in SMALLEST_PARSABLE_BOARD..LARGEST_PARSABLE_BOARD) {
        throw InvalidBoardException("blockSize must be between " +
                "$SMALLEST_PARSABLE_BOARD & $LARGEST_PARSABLE_BOARD" +
                ", found $blockSize")
    }

    val board = Board(blockSize)
    val rawBoard = lines.subList(1, lines.size)
    validateRawBoard(rawBoard, blockSize)

    val cellValues = rawBoard.flatMap { it.map { decodeValueFrom(it) } }
    for ((value, cell) in cellValues.zip(board.cells)) {
        if (value == null) {
            continue
        }

        cell.copyChoices(value)
    }

    return board
}

fun decodeValueFrom(c: Char): Int? {
    val i = VALID_CHARMAP[c.toLowerCase()]

    return when(i) {
        0 -> null
        in 1 .. LARGEST_BASE -> i

        else -> {
            assert(value = false) { "$c not in base $LARGEST_BASE nor is it '${Cell.EMPTY}'" }
            -1
        }

    }
}

fun isValidChar(c: Char, size: Int): Boolean = VALID_CHARMAP[c.toLowerCase()] in (0 .. size)

/**
 * validates input board (already parsed into list of strings)
 * only checks that each row, and column is the correct size, and checks all entries are the right base
 * DOES NOT CHECK THAT THE BOARD IS SOLVABLE!
 *
 * @param lines input board, split on new lines
 * @param blockSize width of each block on the board. If blockSize = n, then boards width (and height) = n*n (n^4 total)
 */
fun validateRawBoard(lines: List<String>, blockSize: Int) {
    val size = blockSize*blockSize

    val height = lines.size
    if (height != size) {
        throw InvalidBoardException("Invalid board, height $height inconsistent with size $size")
    }

    val widthsValid = lines.all { it.length == size }
    if (!widthsValid) {
        throw InvalidBoardException("Invalid board, width $widthsValid inconsistent with size size $size")
    }

    val chars = lines.all {
        it.all { isValidChar(it, size) }
    }
    if (!chars) {
        throw InvalidBoardException("Invalid board, one or more cells not in base $size. Base 36 is [0..9|a..z|A..Z]")
    }
}