package com.yes4all.service.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String sku;

    private String productTitle;

    private String asin;
}
