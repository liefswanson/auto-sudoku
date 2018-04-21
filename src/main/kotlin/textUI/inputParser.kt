package textUI

const val prompt = "> "

fun mapInputToCommand(input: String): Command? =
        try {
            Command.valueOf(input.toLowerCase().capitalize())
        } catch (e: IllegalArgumentException) {
            null
        }

fun readInputFromUser(msg: String): String {
    print("$msg $prompt")
    return readLine() ?: throw EndOfInputException("End of input reached; fix the input file and try again.")
}

class EndOfInputException(msg: String) : Exception(msg)