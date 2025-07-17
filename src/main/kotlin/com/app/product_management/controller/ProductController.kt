package com.app.product_management.controller

import com.app.product_management.dto.ProductForm
import com.app.product_management.dto.VariantForm
import com.app.product_management.model.Product
import com.app.product_management.model.ProductVariants
import com.app.product_management.service.ProductService
import com.app.product_management.service.ProductVariantsService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.stream.IntStream

@Controller
class ProductController(
    private val productVariantsService: ProductVariantsService,
    private val productService: ProductService
) {

    companion object {
        private const val PAGE_SIZE = 10
        private const val FIRST_PAGE = 0
    }

    @GetMapping("/page/{pageNo}")
    fun getPage(
        @PathVariable pageNo: Int,
        @RequestParam(value = "search", required = false) search: String?,
        model: Model
    ): String {
        val page: Page<ProductVariants> = if (!search.isNullOrBlank() && search != "null") {
            productVariantsService.searchByProductTitle(search, pageNo - 1, PAGE_SIZE)
        } else {
            productVariantsService.findPaginated(pageNo - 1, PAGE_SIZE)
        }

        handlePage(model, page, pageNo)
        model.addAttribute("search", search)
        return "fragments/table :: productTable"
    }

    @GetMapping("/")
    fun showForm(model: Model): String {
        val productForm = ProductForm()
        productForm.variants.add(VariantForm())
        model.addAttribute("productForm", productForm)
        return "index"
    }

    @PostMapping("/products")
    fun addProduct(
        @Valid @ModelAttribute("productForm") productForm: ProductForm,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            if (productForm.variants.isNullOrEmpty()) {
                productForm.variants = mutableListOf(VariantForm())
            }
            model.addAttribute("productForm", productForm)
            return "fragments/productForm :: productForm"
        }

        val product = Product().apply {
            id = generateUniqueId()
            title = productForm.title
            productType = productForm.productType
        }

        val productVariantsEntities = productForm.variants.map {
            ProductVariants().apply {
                id = generateUniqueId()
                title = it.title
                price = it.price
                imageSrc = it.imageSrc
                this.product = product
            }
        }

        product.variants = productVariantsEntities.toMutableList()
        productService.save(product)

        val newProductForm = ProductForm().apply {
            variants.add(VariantForm())
        }
        model.addAttribute("productForm", newProductForm)

        val page = productVariantsService.findPaginated(FIRST_PAGE, PAGE_SIZE)
        handlePage(model, page, FIRST_PAGE)

        return "fragments/productForm :: productForm"
    }

    @DeleteMapping("/product-variants/{id}")
    fun deleteProduct(@PathVariable id: Long, model: Model): String {
        productVariantsService.deleteById(id)
        val page = productVariantsService.findPaginated(FIRST_PAGE, PAGE_SIZE)
        handlePage(model, page, FIRST_PAGE)
        return "fragments/table :: productTable"
    }

    @PutMapping("/products/{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestParam title: String,
        @RequestParam productType: String,
        @RequestParam variantTitles: List<String>,
        @RequestParam prices: List<String>,
        @RequestParam imageUrls: List<String>
    ): ResponseEntity<String> {
        val product = productService.findById(productId)
            .orElseThrow { RuntimeException("Product not found with ID: $productId") }

        product.title = title
        product.productType = productType

        val updatedVariants = mutableListOf<ProductVariants>()

        for (i in variantTitles.indices) {
            val variantTitle = variantTitles[i]
            val price = prices[i]
            val imageUrl = imageUrls[i]

            val variant = productVariantsService.findByTitleAndProductId(variantTitle, productId)
                .orElse(ProductVariants())

            variant.title = variantTitle
            variant.price = BigDecimal(price)
            variant.imageSrc = imageUrl
            variant.product = product

            updatedVariants.add(variant)
        }

        product.variants = updatedVariants
        productService.save(product)

        return ResponseEntity.ok("Product and variants updated successfully.")
    }

    @GetMapping("/products/{productId}/variants/{variantId}")
    fun getProductVariantDetails(
        @PathVariable productId: Long,
        @PathVariable variantId: Long
    ): ResponseEntity<ProductVariants> {
        val variant = productVariantsService.findById(variantId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found with ID: $variantId") }

        if (variant.product?.id != productId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Variant does not belong to the specified product.")
        }

        return ResponseEntity.ok(variant)
    }

    @PutMapping("/products/{productId}/variants/{variantId}")
    fun updateProductVariant(
        @PathVariable productId: Long,
        @PathVariable variantId: Long,
        @RequestBody updatedVariant: ProductVariants
    ): ResponseEntity<ProductVariants> {
        val existingVariant = productVariantsService.findById(variantId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found with ID: $variantId") }

        if (existingVariant.product?.id != productId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Variant does not belong to the specified product.")
        }

        existingVariant.title = updatedVariant.title
        existingVariant.price = updatedVariant.price
        existingVariant.imageSrc = updatedVariant.imageSrc

        val saved = productVariantsService.save(existingVariant)
        return ResponseEntity.ok(saved)
    }

    @GetMapping("/products")
    fun getProducts(
        @RequestParam(value = "search", required = false) search: String?,
        @RequestParam(value = "page", defaultValue = "1") pageNo: Int,
        model: Model
    ): String {
        val page = if (!search.isNullOrBlank()) {
            productVariantsService.searchByProductTitle(search, pageNo - 1, PAGE_SIZE)
        } else {
            productVariantsService.findPaginated(pageNo - 1, PAGE_SIZE)
        }

        handlePage(model, page, pageNo)
        model.addAttribute("search", search)
        return "fragments/table :: productTable"
    }

    private fun handlePage(model: Model, page: Page<ProductVariants>, pageNo: Int) {
        val totalPages = page.totalPages
        var start = (pageNo - 2).coerceAtLeast(1)
        var end = (pageNo + 2).coerceAtMost(totalPages)

        if (end - start < 4) {
            if (start == 1) {
                end = (start + 4).coerceAtMost(totalPages)
            } else if (end == totalPages) {
                start = (end - 4).coerceAtLeast(1)
            }
        }

        val pageNumbers = IntStream.rangeClosed(start, end).boxed().toList()

        model.addAttribute("products", page.content)
        model.addAttribute("currentPage", if (pageNo <= 0) 1 else pageNo)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("totalItems", page.totalElements)
        model.addAttribute("pageNumbers", pageNumbers)
    }

    private fun generateUniqueId(): Long {
        return BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toLong()
    }
}