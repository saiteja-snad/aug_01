package com.quickbite.service.impl;

import com.quickbite.dto.request.AddToCartRequest;
import com.quickbite.dto.request.UpdateCartItemRequest;
import com.quickbite.dto.response.CartItemResponse;
import com.quickbite.dto.response.CartResponse;
import com.quickbite.entity.Cart;
import com.quickbite.entity.CartItem;
import com.quickbite.entity.MenuItem;
import com.quickbite.entity.User;
import com.quickbite.exception.BadRequestException;
import com.quickbite.exception.ResourceNotFoundException;
import com.quickbite.repository.CartItemRepository;
import com.quickbite.repository.CartRepository;
import com.quickbite.repository.MenuItemRepository;
import com.quickbite.repository.UserRepository;
import com.quickbite.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(String email, AddToCartRequest request) {
        Cart cart = getOrCreateCart(email);
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + request.getMenuItemId()));

        if (!menuItem.isAvailable()) {
            throw new BadRequestException("Menu item is currently unavailable: " + menuItem.getName());
        }

        // Cart is restaurant-scoped: switching restaurants clears existing items.
        if (cart.getRestaurant() != null && !cart.getRestaurant().getId().equals(menuItem.getRestaurant().getId())) {
            log.info("Cart restaurant switch for {} — clearing previous items", email);
            cart.getItems().clear();
        }
        cart.setRestaurant(menuItem.getRestaurant());

        CartItem existing = cartItemRepository.findByCartAndMenuItem(cart, menuItem).orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartItemRepository.save(existing);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
        }

        cart = cartRepository.save(cart);
        log.info("Item '{}' added to cart for {}", menuItem.getName(), email);
        return toResponse(cart);
    }

    @Override
    public CartResponse updateItem(String email, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        log.info("Cart item {} quantity updated for {}", cartItemId, email);
        return toResponse(cart);
    }

    @Override
    public CartResponse removeItem(String email, Long cartItemId) {
        Cart cart = getOrCreateCart(email);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        if (cart.getItems().isEmpty()) {
            cart.setRestaurant(null);
        }
        cart = cartRepository.save(cart);
        log.info("Cart item {} removed for {}", cartItemId, email);
        return toResponse(cart);
    }

    @Override
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cart.setRestaurant(null);
        cartRepository.save(cart);
        log.info("Cart cleared for {}", email);
    }

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(i -> CartItemResponse.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItem().getName())
                        .price(i.getMenuItem().getPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getMenuItem().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .restaurantId(cart.getRestaurant() != null ? cart.getRestaurant().getId() : null)
                .restaurantName(cart.getRestaurant() != null ? cart.getRestaurant().getName() : null)
                .items(itemResponses)
                .totalAmount(total)
                .build();
    }
}
