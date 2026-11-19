package com.enigmaticdevs.wallhaven.ui.util


fun aspectRatio(dimX: Int, dimY: Int): Double {
    if (dimY <= 0 || dimX <= 0) return 1.0
    return dimX.toDouble() / dimY.toDouble()
}