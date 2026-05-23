package com.example.organizadorapps

data class ExpandableCategoryItem(
    val category: AppCategory,
    var isExpanded: Boolean = false
)