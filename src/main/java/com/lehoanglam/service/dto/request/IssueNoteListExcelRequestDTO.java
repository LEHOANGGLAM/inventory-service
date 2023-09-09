package com.yes4all.service.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteListExcelRequestDTO {
    private String searchBy;
    private String searchValue;
    private Long warehouseId;
    private String fromDate;
    private String toDate;
    private boolean flgAll;
    private String codes;
    private List<String> listColumn;
}
