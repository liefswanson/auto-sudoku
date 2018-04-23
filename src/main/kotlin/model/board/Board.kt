package model.board

class Board (
        private val blockSize: Int =  DEFAULT_SIZE
): Iterable<Cell> {

    val size = blockSize * blockSize
    val cells: Array<Cell>
        get() = field.clone()
    val rows: List<BoardPartition> by lazy {
        first().colCells.map { it.rowCells }
    }
    val cols: List<BoardPartition> by lazy {
        first().rowCells.map { it.colCells }
    }
    val blocks: List<BoardPartition> by lazy {
        topLeftCells().map { it.blockCells }
    }

    companion object {
        const val DEFAULT_SIZE = 3
        private const val VERTICAL_CHAR = '│'
        private const val HORIZONTAL_CHAR = '─'
        private const val CROSS_CHAR = '┼'
    }

    init {
        val rows = initRows()
        val cols = initCols()
        val blocks = initBlocks()

        cells = Array(size*size) {
            val row = it / size
            val col = it % size

            assert( blocks[Location(row, col)] != null, {"($row,$col) is null in block map!"})

            val loc = Location(row, col)
            val block = blocks[loc]!! // should always be safe, as the map is designed only for this purpose
            val cell = Cell(
                    row, col,
                    (1 .. size).toMutableSet(),
                    rows[row],
                    cols[col],
                    block)

            rows[row].add(cell)
            cols[col].add(cell)
            block.add(cell)

            cell
        }
    }

    public fun isSolved(): Boolean = all { cell -> cell.isSolved() }

    private fun initRows(): Array<MutableList<Cell>> = Array(size) {ArrayList<Cell>()}
    private fun initCols(): Array<MutableList<Cell>> = initRows()

    private fun initBlocks(): Map<Location, MutableList<Cell>>  {
        val map = HashMap<Location, ArrayList<Cell>>()

        for (yth in 0 until blockSize) {
            val row = blockSize*yth

            for (xth in 0 until blockSize) {
                val col = blockSize*xth

                val target = ArrayList<Cell>(size)
                val block = buildBlock(row, col)
                block.forEach { map[it] = target }
            }
        }

        return map
    }

    private fun buildBlock(top: Int, left: Int): List<Location> =
            (0 until blockSize).flatMap {
                row -> (0 until blockSize).map {
                    col -> Location(top+row, left+col)
                }
            }

    private fun onBoard(row: Int, col: Int): Boolean =
            row in 0 until size &&
            col in 0 until size

    operator fun get(row: Int, col: Int): Cell {
        handleBoardRange(row, col)
        return cells[row*size + col]
    }

    operator fun set(row: Int, col: Int, elem: Cell){
        handleBoardRange(row, col)
        cells[row*size + col] = elem
    }

    override fun iterator(): Iterator<Cell> = cells.iterator()

    private fun topLeftCells(): BoardPartition =
            (0 until blockSize).flatMap {
                row -> (0 until blockSize).map {

                col -> this[row*blockSize, col*blockSize]
            }}

    fun copy():Board {
        val result = Board(blockSize)
        val cellMap = cells.zip(result.cells)

        for ((source, target) in cellMap) {
            target.copyChoices(source)
        }

        return result
    }

    fun toPrettyString(): String {
        val builder = StringBuilder()

        for (row in 0 until size) {
            if (onBlockEdge(row)) {
                appendLineTo(builder)
            }

            appendRowTo(row, builder)
        }

        return builder.toString()
    }

    private fun onBlockEdge(i: Int): Boolean = (i % blockSize == 0) && (i != 0)

    private fun appendLineTo(builder: StringBuilder) {
        val blockEdge = "".padStart(blockSize, HORIZONTAL_CHAR)

        builder.append(blockEdge)

        for (i in 1 until blockSize) {
            builder.append(CROSS_CHAR)
            builder.append(blockEdge)
        }

        builder.append('\n')
    }

    private fun appendRowTo(row: Int, builder: StringBuilder) {
        for (col in 0 until size) {
            if (onBlockEdge(col)) {
                builder.append(VERTICAL_CHAR)
            }

            builder.append( this[row,col].toPrettyString() )
        }
        builder.append('\n')
    }

    private fun handleBoardRange(row: Int, col: Int) {
        if (!onBoard(row, col)) {
            throw ArrayIndexOutOfBoundsException("($row, $col) not inside (0..${size-1}, 0..${size-1})")
        }
    }

    override fun hashCode(): Int = blockSize*31 + cells.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is Board) return false
        return other.blockSize == blockSize &&
                other.cells.contentEquals(cells)
    }
}

