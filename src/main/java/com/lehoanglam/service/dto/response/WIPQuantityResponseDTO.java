package com.yes4all.service.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WIPQuantityResponseDTO {

    private Boolean success;

    private List<WIPQuantityItemDTO> message;
}
