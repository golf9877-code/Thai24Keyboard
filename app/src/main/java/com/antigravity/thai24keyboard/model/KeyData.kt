package com.antigravity.thai24keyboard.model

/**
 * Data model for a keyboard button.
 * Supports multi-tap cycling:
 * 1st tap -> primary
 * 2nd tap -> secondary (within timeout)
 * 3rd tap -> tertiary
 * 4th tap -> quaternary (if available)
 */
data class KeyData(
    val id: Int,
    val primary: String,
    val secondary: String? = null,
    val tertiary: String? = null,
    val quaternary: String? = null
) {
    /**
     * Get character by cycle index
     */
    fun getCharForCycle(index: Int): String {
        val list = listOfNotNull(primary, secondary, tertiary, quaternary)
        if (list.isEmpty()) return ""
        return list[index % list.size]
    }

    val totalVariations: Int
        get() = listOfNotNull(primary, secondary, tertiary, quaternary).size
}

enum class KeyboardMode {
    THAI_24,
    ENGLISH,
    SYMBOLS
}

object KeyboardLayouts {

    /**
     * Thai 24-Key Compact Layout (3 rows x 8 keys = exactly 24 keys)
     * All 44 consonants + all vowels & tone marks.
     */
    val thai24Rows: List<List<KeyData>> = listOf(
        // Row 1 (8 keys)
        listOf(
            KeyData(id = 101, primary = "ไ", secondary = "ๆ", tertiary = "ๅ"),
            KeyData(id = 102, primary = "ำ", secondary = "ฎ", tertiary = "ฑ"),
            KeyData(id = 103, primary = "พ", secondary = "ภ", tertiary = "ธ"),
            KeyData(id = 104, primary = "ะ", secondary = "ั", tertiary = "ํ"),
            KeyData(id = 105, primary = "ิ", secondary = "ี", tertiary = "ื"),
            KeyData(id = 106, primary = "ุ", secondary = "ู", tertiary = "ึ"),
            KeyData(id = 107, primary = "ร", secondary = "ณ", tertiary = "ฤ"),
            KeyData(id = 108, primary = "น", secondary = "ย", tertiary = "ญ")
        ),
        // Row 2 (8 keys)
        listOf(
            KeyData(id = 201, primary = "ฟ", secondary = "ฝ"),
            KeyData(id = 202, primary = "ห", secondary = "ฆ", tertiary = "ฮ"),
            KeyData(id = 203, primary = "ก", secondary = "ข", tertiary = "ค", quaternary = "ฃ"),
            KeyData(id = 204, primary = "ด", secondary = "ต", tertiary = "ฏ", quaternary = "ฐ"),
            KeyData(id = 205, primary = "เ", secondary = "แ", tertiary = "โ", quaternary = "ใ"),
            KeyData(id = 206, primary = "้", secondary = "่", tertiary = "๊", quaternary = "๋"),
            KeyData(id = 207, primary = "า", secondary = "ส", tertiary = "ศ", quaternary = "ษ"),
            KeyData(id = 208, primary = "ว", secondary = "ซ", tertiary = "ฌ")
        ),
        // Row 3 (8 keys)
        listOf(
            KeyData(id = 301, primary = "ผ", secondary = "ป"),
            KeyData(id = 302, primary = "อ", secondary = "ฯ", tertiary = "ฺ"),
            KeyData(id = 303, primary = "ท", secondary = "ถ", tertiary = "ฒ"),
            KeyData(id = 304, primary = "ม", secondary = "บ"),
            KeyData(id = 305, primary = "ล", secondary = "ฬ"),
            KeyData(id = 306, primary = "ง", secondary = "จ", tertiary = "ช", quaternary = "ฉ"),
            KeyData(id = 307, primary = "์", secondary = "็"),
            KeyData(id = 308, primary = "ฅ", secondary = "฿", tertiary = "ๆ")
        )
    )

    /**
     * English QWERTY
     */
    val englishRows: List<List<KeyData>> = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").mapIndexed { i, c ->
            KeyData(id = 400 + i, primary = c, secondary = c.uppercase())
        },
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").mapIndexed { i, c ->
            KeyData(id = 420 + i, primary = c, secondary = c.uppercase())
        },
        listOf("z", "x", "c", "v", "b", "n", "m").mapIndexed { i, c ->
            KeyData(id = 440 + i, primary = c, secondary = c.uppercase())
        }
    )

    /**
     * Numbers and Symbols
     */
    val symbolRows: List<List<KeyData>> = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").mapIndexed { i, c ->
            KeyData(id = 500 + i, primary = c)
        },
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/").mapIndexed { i, c ->
            KeyData(id = 520 + i, primary = c)
        },
        listOf("*", "\"", "'", ":", ";", "!", "?", "_", "=", "~").mapIndexed { i, c ->
            KeyData(id = 540 + i, primary = c)
        }
    )
}
