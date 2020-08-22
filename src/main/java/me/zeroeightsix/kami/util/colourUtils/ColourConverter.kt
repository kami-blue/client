package me.zeroeightsix.kami.util.colourUtils

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 04/03/20
 * Updated by Xiaro on 09/08/20
 */
object ColourConverter {
    @JvmStatic
    fun toF(i: Int): Float {
        return i / 255f
    }

    @JvmStatic
    fun toF(d: Double): Float {
        return (d / 255f).toFloat()
    }

    @JvmStatic
    fun rgbToInt(r: Int, g: Int, b: Int, a: Int): Int {
        return r shl 16 or (g shl 8) or b or (a shl 24)
    }

    @JvmStatic
    fun rgbToInt(r: Int, g: Int, b: Int): Int {
        return r shl 16 or (g shl 8) or b
    }

    @JvmStatic
    fun intToRgb(intColor: Int): ColourHolder {
        val r = (intColor shr 16)
        val g = (intColor shr 8 and 255)
        val b = (intColor and 255)
        return ColourHolder(r, g, b)
    }
}