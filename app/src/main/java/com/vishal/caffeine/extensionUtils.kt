package com.vishal.caffeine

fun Int.toMilliSeconds(): Long = (this * 60 * 1000).toLong()
fun Long.toTime() : String = "${getMinutes(this)}:${getSeconds(this)}"

fun getMinutes(milliseconds : Long) : Long = (milliseconds)/(60 * 1000)
fun getSeconds(milliseconds: Long) : Long = ((milliseconds) % (60 * 1000))/(1000)