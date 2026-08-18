package com.quickbite.controller;

import com.quickbite.dto.request.AddToCartRequest;
import com.quickbite.dto.request.UpdateCartItemRequest;
import com.quickbite.dto.response.CartResponse;
import com.quickbite.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Shopping cart operations (customers only)")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "View the current user's cart")
    public ResponseEntity<CartResponse> getCart(Authentication auth) {
        return ResponseEntity.ok(cartService.getCart(auth.getName()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest request, Authentication auth) {
        return ResponseEntity.ok(cartService.addItem(auth.getName(), request));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update the quantity of a cart item")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long cartItemId,
                                                     @Valid @RequestBody UpdateCartItemRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(cartService.updateItem(auth.getName(), cartItemId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartItemId, Authentication auth) {
        return ResponseEntity.ok(cartService.removeItem(auth.getName(), cartItemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear the entire cart")
    public ResponseEntity<Void> clearCart(Authentication auth) {
        cartService.clearCart(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
