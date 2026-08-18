package com.quickbite.repository;

import com.quickbite.entity.Cart;
import com.quickbite.entity.CartItem;
import com.quickbite.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);
}
