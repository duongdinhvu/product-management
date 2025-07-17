package com.app.product_management.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ProductForm(
    @field:NotBlank(message = "Product Title is required")
    var title: String = "",

    @field:NotBlank(message = "Product Type is required")
    var productType: String = "",

    @field:Valid
    var variants: MutableList<VariantForm> = mutableListOf()
)