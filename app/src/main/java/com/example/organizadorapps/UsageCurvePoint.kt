package com.example.organizadorapps

data class UsageCurvePoint(
    val label: String,
    val currentValue: Long,
    val previousValue: Long
)

data class UsageCurveResult(
    val points: List<UsageCurvePoint>,
    val sourceLabel: String,
    val valueLabel: String,
    val currentTotal: Long,
    val previousTotal: Long,
    val isDuration: Boolean
) {
    val hasPrevious: Boolean
        get() = previousTotal > 0L
}
