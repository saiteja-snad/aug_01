package com.quickbite.service;

import com.quickbite.dto.request.RestaurantRequest;
import com.quickbite.dto.response.RestaurantResponse;

import java.util.List;

public interface RestaurantService {
    RestaurantResponse create(RestaurantRequest request, String ownerEmail);
    RestaurantResponse update(Long id, RestaurantRequest request, String ownerEmail);
    void delete(Long id, String ownerEmail);
    RestaurantResponse getById(Long id);
    List<RestaurantResponse> getAll();
    List<RestaurantResponse> search(String keyword);
    List<RestaurantResponse> getMyRestaurants(String ownerEmail);
}
