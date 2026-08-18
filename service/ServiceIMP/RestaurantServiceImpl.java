package com.quickbite.service.impl;

import com.quickbite.dto.request.RestaurantRequest;
import com.quickbite.dto.response.MenuItemResponse;
import com.quickbite.dto.response.RestaurantResponse;
import com.quickbite.entity.MenuItem;
import com.quickbite.entity.Restaurant;
import com.quickbite.entity.User;
import com.quickbite.exception.ResourceNotFoundException;
import com.quickbite.exception.UnauthorizedException;
import com.quickbite.repository.RestaurantRepository;
import com.quickbite.repository.UserRepository;
import com.quickbite.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    public RestaurantResponse create(RestaurantRequest request, String ownerEmail) {
        User owner = getUser(ownerEmail);

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .cuisine(request.getCuisine())
                .address(request.getAddress())
                .phone(request.getPhone())
                .owner(owner)
                .active(true)
                .build();

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant '{}' created by owner {}", restaurant.getName(), ownerEmail);
        return toResponse(restaurant);
    }

    @Override
    public RestaurantResponse update(Long id, RestaurantRequest request, String ownerEmail) {
        Restaurant restaurant = getRestaurant(id);
        assertOwnership(restaurant, ownerEmail);

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant {} updated by {}", id, ownerEmail);
        return toResponse(restaurant);
    }

    @Override
    public void delete(Long id, String ownerEmail) {
        Restaurant restaurant = getRestaurant(id);
        assertOwnership(restaurant, ownerEmail);
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
        log.info("Restaurant {} deactivated by {}", id, ownerEmail);
    }

    @Override
    public RestaurantResponse getById(Long id) {
        return toResponse(getRestaurant(id));
    }

    @Override
    public List<RestaurantResponse> getAll() {
        return restaurantRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantResponse> search(String keyword) {
        return restaurantRepository
                .findByNameContainingIgnoreCaseOrCuisineContainingIgnoreCase(keyword, keyword).stream()
                .filter(Restaurant::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantResponse> getMyRestaurants(String ownerEmail) {
        User owner = getUser(ownerEmail);
        return restaurantRepository.findByOwner(owner).stream()
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

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private RestaurantResponse toResponse(Restaurant r) {
        List<MenuItemResponse> items = r.getMenuItems() == null ? List.of() : r.getMenuItems().stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());

        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .cuisine(r.getCuisine())
                .address(r.getAddress())
                .phone(r.getPhone())
                .rating(r.getRating())
                .active(r.isActive())
                .ownerId(r.getOwner().getId())
                .menuItems(items)
                .build();
    }

    private MenuItemResponse toMenuItemResponse(MenuItem m) {
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
