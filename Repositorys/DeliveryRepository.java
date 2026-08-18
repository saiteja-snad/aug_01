package com.quickbite.repository;

import com.quickbite.entity.Delivery;
import com.quickbite.entity.DeliveryStatus;
import com.quickbite.entity.Order;
import com.quickbite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrder(Order order);
    List<Delivery> findByDeliveryAgent(User agent);
    List<Delivery> findByStatus(DeliveryStatus status);
}
