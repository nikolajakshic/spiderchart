package com.nikola.jakshic.spiderchart

/**
 * @param values An array of float values in the range 0..100.
 * @param color A color value in the form 0xAARRGGBB.
 */
class SpiderData(values: FloatArray, var color: Int) {
    var values: FloatArray = values
        private set(values) {
            for (i in 0 until values.size) {
                var value = values[i]
                if (value < 0) value = 0f
                if (value > 100) value = 100f
                values[i] = value
            }
            field = values
        }
}