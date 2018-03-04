package com.armdroid.polygondataview

import android.graphics.Path
/**
 * Created by Alex Gasparyan on 3/2/2018.
 *
 */

typealias Coordinate = Pair<Float, Float>

fun Path.moveTo(coord: Coordinate) {
    moveTo(coord.first, coord.second)
}

fun Path.lineTo(coord: Coordinate) {
    lineTo(coord.first, coord.second)
}

fun Path.quadTo(controlCoord: Coordinate, endCoord: Coordinate) {
    quadTo(controlCoord.first, controlCoord.second, endCoord.first, endCoord.second)
}
