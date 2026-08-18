package com.quickbite.service;

import com.quickbite.dto.request.AddToCartRequest;
import com.quickbite.dto.request.UpdateCartItemRequest;
import com.quickbite.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String email);
    CartResponse addItem(String email, AddToCartRequest request);
    CartResponse updateItem(String email, Long cartItemId, UpdateCartItemRequest request);
    CartResponse removeItem(String email, Long cartItemId);
    void clearCart(String email);
}
