package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDTO {
    private Integer id;
    private String src;
    private String module;
    private String name;
    private Long size;
    private String type;
}
