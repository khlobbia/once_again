package com.khlob.onceagain

import android.graphics.Paint
import android.util.Log

data class mapObj(var x: Int = 0, var z: Int= 0, var width: Int=0, var height: Int=0, var isPerson: Boolean = false) {
    //UNITS IN MM, LINES ARE 200 MM, SCREEN FOV IS 2 * DIST
    var paint: Paint = Paint()
    fun check_pos(){
        if(isPerson) Log.d("g", "is orng at x="+x+", y="+z)
        if(Math.abs(x) < 800 && Math.abs(z) < 800){
            x = 5000
            z=5000
            height = 100
        }
    }
}