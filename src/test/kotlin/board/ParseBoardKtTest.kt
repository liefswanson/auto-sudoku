package board

import model.board.Cell
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

internal class ParseBoardKtTest {

    @Test
    fun parseBoard() {
    }

    @Test
    fun charMap() {
        assertEquals(36, textUI.decodeValueFrom('0'))
        assertEquals(10, textUI.decodeValueFrom('a'))
        assertEquals(35, textUI.decodeValueFrom('z'))
        assertEquals(10+('j'-'a'), textUI.decodeValueFrom('j'))
        assertEquals(9, textUI.decodeValueFrom('9'))
        assertEquals(10+('b'-'a'), textUI.decodeValueFrom('b'))
        assertEquals(5, textUI.decodeValueFrom('5'))
        assertEquals(10+('m'-'a'), textUI.decodeValueFrom('m'))
        assertEquals(10+('s'-'a'), textUI.decodeValueFrom('s'))
    }

    @Test
    fun validChar() {
        assertTrue(textUI.isValidChar('1', 1))
        assertFalse(textUI.isValidChar('2', 1))

        assertTrue(textUI.isValidChar('4', 4))
        assertFalse(textUI.isValidChar('5', 4))

        assertTrue(textUI.isValidChar('9', 9))
        assertFalse(textUI.isValidChar('a', 9))

        assertTrue(textUI.isValidChar('g', 16))
        assertTrue(textUI.isValidChar('G', 16))
        assertFalse(textUI.isValidChar('H', 16))

        assertTrue(textUI.isValidChar('p', 25))
        assertTrue(textUI.isValidChar('P', 25))
        assertFalse(textUI.isValidChar('q', 25))

        assertTrue(textUI.isValidChar('z', 36))
        assertTrue(textUI.isValidChar('Z', 36))
        assertTrue(textUI.isValidChar('0', 36))
        assertFalse(textUI.isValidChar('$', 36))

        assertTrue(textUI.isValidChar(Cell.EMPTY, 1))
        assertTrue(textUI.isValidChar(Cell.EMPTY, 4))
        assertTrue(textUI.isValidChar(Cell.EMPTY, 9))
        assertTrue(textUI.isValidChar(Cell.EMPTY, 16))
        assertTrue(textUI.isValidChar(Cell.EMPTY, 25))
        assertTrue(textUI.isValidChar(Cell.EMPTY, 36))
    }

    @Test
    fun validateRawBoard() {
    }
}