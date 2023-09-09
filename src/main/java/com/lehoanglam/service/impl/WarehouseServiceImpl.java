package com.yes4all.service.impl;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.Warehouse;
import com.yes4all.repository.WarehouseRepository;
import com.yes4all.service.WarehouseService;
import com.yes4all.service.dto.response.WarehouseResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Warehouse}.
 */
@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final Logger log = LoggerFactory.getLogger(WarehouseServiceImpl.class);

    private final WarehouseRepository warehouseRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> findAll() {
        List<Warehouse> listWarehouse = warehouseRepository.findAll();
        return listWarehouse.stream().map(item -> mappingEntityToDTO(item, WarehouseResponseDTO.class)).collect(Collectors.toList());
    }

    private <T> T mappingEntityToDTO(Warehouse entity, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(entity, dto);

            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }
}
