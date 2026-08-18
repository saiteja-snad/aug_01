package com.quickbite.controller;

import com.quickbite.dto.request.OrderStatusUpdateRequest;
import com.quickbite.dto.request.PlaceOrderRequest;
import com.quickbite.dto.response.OrderResponse;
import com.quickbite.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Order placement, history and status management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place an order from the current cart")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(auth.getName(), request));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details (customer, owning restaurant, admin, or delivery agent)")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId, Authentication auth) {
        return ResponseEntity.ok(orderService.getOrder(orderId, auth.getName()));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get the current customer's order history")
    public ResponseEntity<List<OrderResponse>> getMyHistory(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrderHistory(auth.getName()));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get all orders for a restaurant owned by the caller")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(@PathVariable Long restaurantId, Authentication auth) {
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId, auth.getName()));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @Operation(summary = "Update order status (restaurant owner or admin)")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long orderId,
                                                        @Valid @RequestBody OrderStatusUpdateRequest request,
                                                        Authentication auth) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request, auth.getName()));
    }
}
