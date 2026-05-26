package com.example.organizadorapps.data.entity

data class AppLaunchEntity(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val openedAt: Long,
    val source: String
) {
    companion object {
        const val SOURCE_FROM_APP = "FROM_APP"
        const val SOURCE_FROM_SYSTEM = "FROM_SYSTEM"
    }
}
