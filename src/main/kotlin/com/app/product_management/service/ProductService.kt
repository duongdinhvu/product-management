package com.app.product_management.service

import com.app.product_management.repository.ProductRepository
import com.app.product_management.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    fun save(p: Product): Product {
        return productRepository.save(p)
    }

    fun delete(id: Long) {
        productRepository.deleteById(id)
    }

    fun findPaginated(pageNo: Int, pageSize: Int): Page<Product> {
        val pageable: Pageable = PageRequest.of(
            pageNo,
            pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        )
        return productRepository.findAll(pageable)
    }

    fun count(): Long {
        return productRepository.count()
    }

    fun deleteById(id: Long) {
        productRepository.deleteById(id)
    }

    fun saveProduct(product: Product) {
        productRepository.save(product)
    }

    fun findById(productId: Long): Optional<Product> {
        return productRepository.findById(productId)
    }
}
