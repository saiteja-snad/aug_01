package com.quickbite.service;

import com.quickbite.dto.request.MenuItemRequest;
import com.quickbite.dto.response.MenuItemResponse;

import java.util.List;

public interface MenuItemService {
    MenuItemResponse addItem(Long restaurantId, MenuItemRequest request, String ownerEmail);
    MenuItemResponse updateItem(Long restaurantId, Long itemId, MenuItemRequest request, String ownerEmail);
    void deleteItem(Long restaurantId, Long itemId, String ownerEmail);
    List<MenuItemResponse> getMenu(Long restaurantId);
}
