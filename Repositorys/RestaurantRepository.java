package com.quickbite.repository;

import com.quickbite.entity.Restaurant;
import com.quickbite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByOwner(User owner);
    List<Restaurant> findByActiveTrue();
    List<Restaurant> findByNameContainingIgnoreCaseOrCuisineContainingIgnoreCase(String name, String cuisine);
}
