package com.quickbite.service;

import com.quickbite.dto.request.OrderStatusUpdateRequest;
import com.quickbite.dto.request.PlaceOrderRequest;
import com.quickbite.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(String customerEmail, PlaceOrderRequest request);
    OrderResponse getOrder(Long orderId, String requesterEmail);
    List<OrderResponse> getMyOrderHistory(String customerEmail);
    List<OrderResponse> getRestaurantOrders(Long restaurantId, String ownerEmail);
    OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request, String requesterEmail);
}
