package textUI

enum class Command(message: String) {
    //Undo,
    //Set,
    //Hint,
    //Save,
    //List,
    Solve("attempt to solve current board"),
    Show(" show the current board"),
    Help(" display this message"),
    Quit(" quit the application, discarding current progress");

    val description = this.toString() + ":    " +  message
}