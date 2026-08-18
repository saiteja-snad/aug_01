package com.quickbite.repository;

import com.quickbite.entity.Order;
import com.quickbite.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
}
