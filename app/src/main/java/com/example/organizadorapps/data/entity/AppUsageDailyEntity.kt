package com.example.organizadorapps.data.entity

data class AppUsageDailyEntity(
    val packageName: String,
    val appName: String,
    val usageDate: String,
    val totalUsageMs: Long,
    val openCount: Int,
    val source: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SOURCE_SYSTEM = "SYSTEM"
        const val SOURCE_INTERNAL = "INTERNAL"
    }
}
