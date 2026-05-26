package com.example.organizadorapps.data.entity

data class AppUsageSnapshotEntity(
    val packageName: String,
    val appName: String,
    val lastUsedAt: Long,
    val source: String,
    val updatedAt: Long
)
