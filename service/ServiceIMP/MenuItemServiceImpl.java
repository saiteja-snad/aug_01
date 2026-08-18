package com.quickbite.service.impl;

import com.quickbite.dto.request.MenuItemRequest;
import com.quickbite.dto.response.MenuItemResponse;
import com.quickbite.entity.MenuItem;
import com.quickbite.entity.Restaurant;
import com.quickbite.exception.ResourceNotFoundException;
import com.quickbite.exception.UnauthorizedException;
import com.quickbite.repository.MenuItemRepository;
import com.quickbite.repository.RestaurantRepository;
import com.quickbite.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public MenuItemResponse addItem(Long restaurantId, MenuItemRequest request, String ownerEmail) {
        Restaurant restaurant = getRestaurant(restaurantId);
        assertOwnership(restaurant, ownerEmail);

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .available(request.getAvailable() == null || request.getAvailable())
                .imageUrl(request.getImageUrl())
                .restaurant(restaurant)
                .build();

        item = menuItemRepository.save(item);
        log.info("Menu item '{}' added to restaurant {}", item.getName(), restaurantId);
        return toResponse(item);
    }

    @Override
    public MenuItemResponse updateItem(Long restaurantId, Long itemId, MenuItemRequest request, String ownerEmail) {
        Restaurant restaurant = getRestaurant(restaurantId);
        assertOwnership(restaurant, ownerEmail);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));

        if (!item.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Menu item not found in this restaurant: " + itemId);
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }
        item.setImageUrl(request.getImageUrl());

        item = menuItemRepository.save(item);
        log.info("Menu item {} updated in restaurant {}", itemId, restaurantId);
        return toResponse(item);
    }

    @Override
    public void deleteItem(Long restaurantId, Long itemId, String ownerEmail) {
        Restaurant restaurant = getRestaurant(restaurantId);
        assertOwnership(restaurant, ownerEmail);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));

        menuItemRepository.delete(item);
        log.info("Menu item {} deleted from restaurant {}", itemId, restaurantId);
    }

    @Override
    public List<MenuItemResponse> getMenu(Long restaurantId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        return menuItemRepository.findByRestaurant(restaurant).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void assertOwnership(Restaurant restaurant, String ownerEmail) {
        if (!restaurant.getOwner().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new UnauthorizedException("You do not own this restaurant");
        }
    }

    private Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    private MenuItemResponse toResponse(MenuItem m) {
        return MenuItemResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .category(m.getCategory())
                .available(m.isAvailable())
                .imageUrl(m.getImageUrl())
                .restaurantId(m.getRestaurant().getId())
                .build();
    }
}
