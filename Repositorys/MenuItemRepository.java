package com.quickbite.repository;

import com.quickbite.entity.MenuItem;
import com.quickbite.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);
}
