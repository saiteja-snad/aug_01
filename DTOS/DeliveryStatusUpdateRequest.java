package com.quickbite.dto.request;

import com.quickbite.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private DeliveryStatus status;

    private String currentLocation;
}
