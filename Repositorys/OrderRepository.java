package com.quickbite.repository;

import com.quickbite.entity.Order;
import com.quickbite.entity.Restaurant;
import com.quickbite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
}
