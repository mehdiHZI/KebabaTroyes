package com.amhsolution.caisse.domain.model

data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val priceCentimes: Int
)
