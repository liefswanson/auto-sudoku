package textUI

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class CommandTest{
    @Test
    fun mapTest() {
        assertEquals(mapInputToCommand("solve"), Command.Solve)
        assertEquals(mapInputToCommand("Solve"), Command.Solve)
        assertEquals(mapInputToCommand("SOLVE"), Command.Solve)

        assertEquals(mapInputToCommand("quit"), Command.Quit)
        assertEquals(mapInputToCommand("Quit"), Command.Quit)
        assertEquals(mapInputToCommand("QuIt"), Command.Quit)
    }

    @Test
    fun mapTestExcept() {
        assertNull( mapInputToCommand("abc") )
    }
}