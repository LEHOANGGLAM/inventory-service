package com.yes4all.service;

public interface InventoryLogService<T> {
    void doInsertLog(T item, Long warehouseId);
}
