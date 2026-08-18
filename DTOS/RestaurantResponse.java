package com.quickbite.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String cuisine;
    private String address;
    private String phone;
    private Double rating;
    private boolean active;
    private Long ownerId;
    private List<MenuItemResponse> menuItems;
}
