package com.yes4all.service.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteDTO {

    String issueCode;
    String userName;
}
