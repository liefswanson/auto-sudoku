package textUI

import model.board.Board
import model.board.Cell
import model.board.InvalidBoardException
import model.solver.SimpleSolver
import utils.align
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths
import kotlin.system.exitProcess

private const val SIZE_FLAG = "--size"
private const val BOARD_FLAG = "--board"
private const val HELP_FLAG = "--help"

fun main(args: Array<String>) {
    val inputPathIndex = args.indexOf(BOARD_FLAG) + 1
    val boardSizeIndex = args.indexOf(SIZE_FLAG) + 1

    val sizeFlagActive = boardSizeIndex in 1 until args.size
    val boardFlagActive = inputPathIndex in 1 until args.size
    val helpFlagActive = HELP_FLAG in args

    when {
        helpFlagActive -> printHelpMessageAndExit(exitStatus = 0)
        sizeFlagActive && boardFlagActive-> printHelpMessageAndExit()

        boardFlagActive -> startSudokuFromBoard(args[inputPathIndex])
        sizeFlagActive -> startSudokuFromScratch(args[boardSizeIndex])

        else -> printHelpMessageAndExit()
    }
}

fun startSudokuFromBoard(path: String) {
    val file = try {
        File(path)
    } catch(e: FileNotFoundException) {
        println("Invalid file path ($path) provided to $BOARD_FLAG flag")
        printHelpMessageAndExit()
        return
    }

    val board = try { parseBoard(file.readText()) }
    catch (e: InvalidBoardException) {
        println("Invalid board format in provided file ($path)")
        println(e.message)
        printHelpMessageAndExit()
        return
    }

    startSudoku(board)
}

fun startSudokuFromScratch(sizeString: String) {
    val size = try {
        sizeString.toInt()
    } catch (e: NumberFormatException) {
        0
    }

    if (size !in SMALLEST_PARSABLE_BOARD..LARGEST_PARSABLE_BOARD) {
        printHelpMessageAndExit()
    }


    startSudoku(Board(size))
}


fun startSudoku(board: Board) {
    val solver = SimpleSolver
    val tui = TextUI(solver, board)

    while(true){
        val command = try {
            tui.executeCommand()
        } catch (e: EndOfInputException) {
            println(e.message)
            Command.Quit
        }

        if (command == Command.Quit) { return }
    }
}

private val WORKING_DIR = Paths.get("")
        .toAbsolutePath()
        .toString()
private val PROGRAM_NAME = object {}
        .javaClass
        .protectionDomain
        .codeSource
        .location
        .path
        .replace(WORKING_DIR, ".")

fun printHelpMessageAndExit(exitStatus: Int = 1) {
    val small = SMALLEST_PARSABLE_BOARD
    val large = LARGEST_PARSABLE_BOARD

    val (help, size, board) =
            align(
                    "$HELP_FLAG:",
                    "$SIZE_FLAG <$small..$large>:",
                    "$BOARD_FLAG <path/to/file>:")

    println()
    println("flags:")
    println("$help show this message")
    println("$size create a blank board with block width $small..$large")
    println("$board load board from a file")
    println()

    println("restrictions:")
    println("cannot use multiple flags at once")
    println()

    println("examples:")
    println("java -jar $PROGRAM_NAME --board ~/3x3board.txt")
    println("java -jar $PROGRAM_NAME --size 3")
    println()

    // I ended up changing which character I used for an empty cell a lot...
    // so it seemed best to write the following abomination, rather than the raw strings
    val e = Cell.EMPTY
    println("example format of input board file:")
    println("2")
    println("${e}3$e$e")
    println("$e$e${e}2")
    println("4$e$e$e")
    println("$e${e}1$e")

    exitProcess(exitStatus)
}


