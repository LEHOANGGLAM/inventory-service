package com.yes4all.service;

import com.yes4all.domain.Product;
import com.yes4all.service.dto.ProductDTO;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Service Interface for managing {@link Product}.
 */
public interface ProductService {
    /**
     * Get all the products.
     *
     * @return the list of entities.
     */
    Page<ProductDTO> findAllBySku(String sku);

    List<ProductDTO> findAllProducts();
}
