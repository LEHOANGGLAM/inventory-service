package com.yes4all.service.impl;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.domain.Product;
import com.yes4all.repository.ProductRepository;
import com.yes4all.service.ProductService;
import com.yes4all.service.dto.ProductDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.yes4all.domain.Product}.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<ProductDTO> findAllBySku(String sku) {
        Pageable defaultPageable = PageRequestUtil.genPageRequest(0, 20, Sort.Direction.ASC, "sku");
        Page<Product> result = productRepository.findBySkuContaining(sku, defaultPageable);
        return result.map(item -> {
            ProductDTO dto = new ProductDTO();
            CommonDataUtil.getModelMapper().map(item, dto);
            return dto;
        });
    }

    @Override
    public List<ProductDTO> findAllProducts() {
        List<Product> result = productRepository.findAll();
        return result
            .parallelStream()
            .map(item -> {
                ProductDTO dto = new ProductDTO();
                CommonDataUtil.getModelMapper().map(item, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
