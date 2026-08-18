package com.quickbite.service.impl;

import com.quickbite.dto.request.OrderStatusUpdateRequest;
import com.quickbite.dto.request.PlaceOrderRequest;
import com.quickbite.dto.response.OrderItemResponse;
import com.quickbite.dto.response.OrderResponse;
import com.quickbite.entity.*;
import com.quickbite.exception.BadRequestException;
import com.quickbite.exception.ResourceNotFoundException;
import com.quickbite.exception.UnauthorizedException;
import com.quickbite.repository.CartRepository;
import com.quickbite.repository.OrderRepository;
import com.quickbite.repository.RestaurantRepository;
import com.quickbite.repository.UserRepository;
import com.quickbite.service.NotificationService;
import com.quickbite.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse placeOrder(String customerEmail, PlaceOrderRequest request) {
        User customer = getUser(customerEmail);
        Cart cart = cartRepository.findByUser(customer)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty() || cart.getRestaurant() == null) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        BigDecimal total = cart.getItems().stream()
                .map(ci -> ci.getMenuItem().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(customer)
                .restaurant(cart.getRestaurant())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .notes(request.getNotes())
                .build();

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> OrderItem.builder()
                        .order(order)
                        .menuItem(ci.getMenuItem())
                        .menuItemName(ci.getMenuItem().getName())
                        .quantity(ci.getQuantity())
                        .priceAtOrder(ci.getMenuItem().getPrice())
                        .build())
                .collect(Collectors.toList());

        order.setItems(orderItems);
        order = orderRepository.save(order);

        // Clear cart after successful order placement
        cart.getItems().clear();
        cart.setRestaurant(null);
        cartRepository.save(cart);

        notificationService.notify(customer, "Your order #" + order.getId() + " has been placed successfully.",
                NotificationType.ORDER_PLACED);
        notificationService.notify(order.getRestaurant().getOwner(),
                "New order #" + order.getId() + " received from " + customer.getName(),
                NotificationType.ORDER_PLACED);

        log.info("Order {} placed by {} for restaurant {}", order.getId(), customerEmail, order.getRestaurant().getId());
        return toResponse(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId, String requesterEmail) {
        Order order = getOrderEntity(orderId);
        assertCanView(order, requesterEmail);
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getMyOrderHistory(String customerEmail) {
        User customer = getUser(customerEmail);
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getRestaurantOrders(Long restaurantId, String ownerEmail) {
        User owner = getUser(ownerEmail);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        if (!restaurant.getOwner().getId().equals(owner.getId()) && owner.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You do not own this restaurant");
        }

        return orderRepository.findByRestaurantOrderByCreatedAtDesc(restaurant).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request, String requesterEmail) {
        Order order = getOrderEntity(orderId);
        User requester = getUser(requesterEmail);

        boolean isOwner = order.getRestaurant().getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to update this order's status");
        }

        order.setStatus(request.getStatus());
        order = orderRepository.save(order);

        notificationService.notify(order.getCustomer(),
                "Your order #" + order.getId() + " status changed to " + order.getStatus(),
                NotificationType.ORDER_CONFIRMED);

        log.info("Order {} status updated to {} by {}", orderId, request.getStatus(), requesterEmail);
        return toResponse(order);
    }

    private void assertCanView(Order order, String requesterEmail) {
        User requester = getUser(requesterEmail);
        boolean isCustomer = order.getCustomer().getId().equals(requester.getId());
        boolean isOwner = order.getRestaurant().getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isDeliveryAgent = requester.getRole() == Role.DELIVERY_AGENT;
        if (!isCustomer && !isOwner && !isAdmin && !isDeliveryAgent) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }
    }

    private Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItemName())
                        .quantity(i.getQuantity())
                        .priceAtOrder(i.getPriceAtOrder())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
