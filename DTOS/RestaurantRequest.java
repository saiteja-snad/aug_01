package com.quickbite.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    private String cuisine;

    @NotBlank(message = "Address is required")
    private String address;

    private String phone;
}
