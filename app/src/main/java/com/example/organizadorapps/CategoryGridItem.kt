package com.example.organizadorapps

sealed class CategoryGridItem {

    data class Folder(
        val item: ExpandableCategoryItem
    ) : CategoryGridItem()

    data class ExpandedPanel(
        val item: ExpandableCategoryItem
    ) : CategoryGridItem()
}