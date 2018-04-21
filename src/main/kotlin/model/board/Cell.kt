package model.board

class Cell (
        val row: Int,
        val col: Int,
        val possibilities: MutableSet<Int>,
        rowCells: List<Cell>,
        colCells: List<Cell>,
        blockCells: List<Cell>
) :Iterable<Int> {

    val rowCells = rowCells
        get() = field.toList()
    val colCells = colCells
        get() = field.toList()
    val blockCells = blockCells
        get() = field.toList()

    companion object {
        const val EMPTY = '.'
        const val UNRESOLVED = '*'
    }

    fun toPrettyString(): String =
            when(possibilities.size) {
                0 -> "$EMPTY"
                1 -> possibilities.first().toString()
                else -> "$UNRESOLVED"
            }

    override fun iterator(): Iterator<Int> = possibilities.iterator()

    operator fun component1():Int = row
    operator fun component2():Int = col

    override fun equals(other: Any?): Boolean {
        if (other == null)               { return false }
        if (this::class != other::class) { return false }

        val cell = other as Cell

        return  this.row == cell.row &&
                this.col == cell.col &&
                this.possibilities == cell.possibilities

    }

    override fun hashCode(): Int {
        var result = row
        result = 31 * result + col
        result = 31 * result + possibilities.hashCode()
        return result
    }

    fun isImpossible(): Boolean = possibilities.isEmpty()

    fun isSolved(): Boolean = possibilities.size == 1
    fun isNotSolved(): Boolean = !isSolved()

    fun onlyPossibility(): Int? = if (isSolved()) possibilities.first() else null

    fun copyChoices(vararg possibilities: Int) {
        this.possibilities.clear()
        this.possibilities.addAll(possibilities.toList())
    }

    fun copyChoices(that: Cell) {
        this.possibilities.clear()
        this.possibilities.addAll(that.possibilities)
    }

    fun remove(possibility: Int): Boolean = this.possibilities.remove(possibility)
    fun removeAll(possibilities: Collection<Int>): Boolean = this.possibilities.removeAll(possibilities)
}