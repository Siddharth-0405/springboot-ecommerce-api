package com.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
}
