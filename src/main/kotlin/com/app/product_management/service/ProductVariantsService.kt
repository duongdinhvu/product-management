package com.app.product_management.service

import com.app.product_management.model.ProductVariants
import com.app.product_management.repository.ProductVariantsRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.Optional
import org.slf4j.Logger


@Service
class ProductVariantsService(
    private val productVariantsRepository: ProductVariantsRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(ProductVariantsService::class.java) as Logger

    fun save(productVariants: ProductVariants): ProductVariants {
        return productVariantsRepository.save(productVariants)
    }

    fun findPaginated(pageNo: Int, pageSize: Int): Page<ProductVariants> {
        val pageable: Pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        return productVariantsRepository.findAll(pageable)
    }

    fun deleteById(id: Long) {
        productVariantsRepository.deleteById(id)
        logger.info("Successfully initiated delete for product variant with ID: $id",)
        if (!productVariantsRepository.existsById(id)) {
            logger.info("Confirmed deletion of product variant with ID: $id")
        }
    }

    fun count(): Long {
        return productVariantsRepository.count()
    }

    fun searchByProductTitle(search: String, pageIndex: Int, pageSize: Int): Page<ProductVariants> {
        val pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        return productVariantsRepository.findByProductTitleContainingIgnoreCase(search, pageable)
    }

    fun findById(variantId: Long): Optional<ProductVariants> {
        return productVariantsRepository.findById(variantId)
    }

    fun findByTitleAndProductId(variantTitle: String, productId: Long): Optional<ProductVariants> {
        return productVariantsRepository.findByTitleAndProductId(variantTitle, productId)
    }

}