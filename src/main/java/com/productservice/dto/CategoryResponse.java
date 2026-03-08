package com.productservice.dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private int productCount;
    private LocalDateTime createdAt;
}
