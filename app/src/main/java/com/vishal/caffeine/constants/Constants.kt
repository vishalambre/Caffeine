package com.vishal.caffeine.constants

const val STOPPED_CAFFEINE = "STOPPED"
const val MINUTE_TWO = "TWO"
const val MINUTE_FIVE = "FIVE"
const val MINUTE_TEN = "TEN"
const val MINUTE_THIRTY = "THIRTY"
const val MINUTE_SIXTY = "SIXTY"
const val INFINITY = "INFINITY"
const val INFINITY_UNICODE = "\u221E"


val valueMap = mapOf(
    STOPPED_CAFFEINE to 0,
    INFINITY to -1,
    MINUTE_TWO to 2,
    MINUTE_FIVE to 5,
    MINUTE_TEN to 10,
    MINUTE_THIRTY to 30,
    MINUTE_SIXTY to 60
)

fun getNewState(currState: String) = when (currState) {
    STOPPED_CAFFEINE -> INFINITY
    INFINITY -> MINUTE_TWO
    MINUTE_TWO -> MINUTE_FIVE
    MINUTE_FIVE -> MINUTE_TEN
    MINUTE_TEN -> MINUTE_THIRTY
    MINUTE_THIRTY -> MINUTE_SIXTY
    else -> STOPPED_CAFFEINE
}
