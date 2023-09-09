package com.yes4all.service.mapper;

import com.yes4all.domain.Product;
import com.yes4all.service.dto.response.ProductResponseDTO;

public class ProductMapper {

    public ProductResponseDTO mapEntityToDto(Product product) {
        return ProductResponseDTO.builder().sku(product.getSku()).id(product.getId()).build();
    }
}
