package com.app.product_management.repository

import com.app.product_management.model.ProductVariants
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProductVariantsRepository : JpaRepository<ProductVariants, Long>{

    fun findByTitleAndProductId(variantTitle: String, productId: Long): Optional<ProductVariants>
    fun findByProductTitleContainingIgnoreCase(title: String, pageable: Pageable): Page<ProductVariants>
}