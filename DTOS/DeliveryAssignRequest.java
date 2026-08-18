package com.quickbite.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryAssignRequest {

    @NotNull(message = "Delivery agent id is required")
    private Long agentId;
}
