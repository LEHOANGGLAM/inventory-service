package com.yes4all.web.rest.controller;

import com.yes4all.service.ProductService;
import com.yes4all.service.dto.ProductDTO;
import com.yes4all.web.rest.payload.RestResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Transactional
public class ProductResource {

    private final Logger log = LoggerFactory.getLogger(ProductResource.class);

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<RestResponse<Object>> getAll() {
        log.debug("REST request to get all Product");
        List<ProductDTO> data = productService.findAllProducts();
        return ResponseEntity.status(HttpStatus.OK).body(RestResponse.builder().body(data).build());
    }
}
