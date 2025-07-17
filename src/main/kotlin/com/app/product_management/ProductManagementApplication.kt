package com.app.product_management

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class ProductManagementApplication

fun main(args: Array<String>) {
	runApplication<ProductManagementApplication>(*args)
}
