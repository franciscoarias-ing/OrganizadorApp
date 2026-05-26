package com.example.organizadorapps

data class UsageAppStat(
    val app: InstalledApp,
    val openCount: Int,
    val lastOpenedAt: Long,
    val detailText: String? = null
)