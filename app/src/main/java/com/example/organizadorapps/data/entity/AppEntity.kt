package com.example.organizadorapps.data.entity

data class AppEntity(
    val packageName: String,
    val name: String,
    val isHidden: Boolean,
    val isInstalled: Boolean,
    val lastSeenAt: Long,
    val updatedAt: Long
)
