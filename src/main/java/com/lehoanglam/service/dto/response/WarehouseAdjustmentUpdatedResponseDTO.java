package com.yes4all.service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseAdjustmentUpdatedResponseDTO {

    private String status;
    private String summary;
    private String description;
    private String reason;
    private DataResponse dataResponse;

    @Getter
    @Setter
    @Builder
    public static class DataResponse {

        private boolean success;
        private String msg;
        private Integer wipSuccess;
        private Integer pkuSuccess;
    }
}
