package com.app.product_management.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal

data class VariantForm(
    @field:NotBlank(message = "Variant Title is required")
    var title: String = "",

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    var price: BigDecimal = BigDecimal("0.01"),

    @field:NotBlank(message = "Image URL is required")
    @field:Pattern(
        regexp = "^(http|https)://.*$",
        message = "Image URL must be a valid URL"
    )
    var imageSrc: String = ""
)