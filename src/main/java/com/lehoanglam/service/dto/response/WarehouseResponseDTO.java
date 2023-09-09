package com.yes4all.service.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponseDTO {
    private Long id;
    private String warehouseName;
    private String warehouseCode;
    private String address;
}
