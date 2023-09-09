package com.yes4all.service.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteListRequestDTO {

    private Integer page;
    private Integer size;
    private String searchBy;
    private String searchValue;
    private Long warehouseId;
    private String fromDate;
    private String toDate;
}
