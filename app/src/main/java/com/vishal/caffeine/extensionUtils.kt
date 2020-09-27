package com.vishal.caffeine

import androidx.annotation.CheckResult

@CheckResult
fun Int.toMilliSeconds(): Long = (this * 60 * 1000).toLong()

@CheckResult
fun Long.toTime(): String = "${getMinutes(this)}:${String.format("%02d", getSeconds(this))}"

@CheckResult
fun getMinutes(milliseconds: Long): Long = (milliseconds) / (60 * 1000)

@CheckResult
fun getSeconds(milliseconds: Long): Long = ((milliseconds) % (60 * 1000)) / (1000)