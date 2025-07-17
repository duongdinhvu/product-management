package com.app.product_management.component

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.app.product_management.model.Product
import com.app.product_management.model.ProductVariants
import com.app.product_management.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Component
class ProductDataLoader(
    private val productService: ProductService,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(ProductDataLoader::class.java)
    private val maxVariants = 50
    private val url = "https://famme.no/products.json"

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
    fun loadInitialData() {
        if (productService.count() > 0) return

        val response: ResponseEntity<String> = restTemplate.getForEntity(url, String::class.java)

        if (response.statusCode.is2xxSuccessful) {
            val body = response.body ?: run {
                logger.error("Empty response body from $url")
                return
            }

            val root: JsonNode = objectMapper.readTree(body)
            val products = root.get("products") ?: run {
                logger.error("No 'products' node found in response")
                return
            }

            if (products.isArray) {
                var count = 0

                for (productNode in products) {
                    val productId = productNode.get("id").asLong()
                    val productTitle = productNode.get("title").asText()
                    val productType = productNode.get("product_type").asText()

                    val product = Product().apply {
                        id = productId
                        title = productTitle
                        this.productType = productType
                    }

                    val variants = productNode.get("variants") ?: continue
                    val variantList = mutableListOf<ProductVariants>()

                    for (variant in variants) {
                        if (count >= maxVariants) break

                        val id = variant.get("id").asLong()
                        val title = variant.get("title").asText()
                        val priceString = variant.get("price").asText()
                        val price = try {
                            BigDecimal(priceString)
                        } catch (ex: NumberFormatException) {
                            logger.error("Invalid price: $priceString", ex)
                            null
                        }

                        val imageUrl = variant.get("featured_image")?.get("src")?.asText() ?: ""

                        val pv = ProductVariants().apply {
                            this.id = id
                            this.title = title
                            this.price = price
                            this.imageSrc = imageUrl
                            this.product = product
                        }
                        variantList.add(pv)
                        count++
                    }

                    product.variants = variantList
                    productService.save(product)
                }
            }
        } else {
            logger.error("Failed to fetch products: ${response.statusCode}")
        }
    }
}
