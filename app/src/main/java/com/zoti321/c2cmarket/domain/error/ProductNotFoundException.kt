package com.zoti321.c2cmarket.domain.error

class ProductNotFoundException(val productId: Int) :
    Exception("Product not found: $productId")
