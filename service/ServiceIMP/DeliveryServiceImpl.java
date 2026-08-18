package com.quickbite.service.impl;

import com.quickbite.dto.request.DeliveryAssignRequest;
import com.quickbite.dto.request.DeliveryStatusUpdateRequest;
import com.quickbite.dto.response.DeliveryResponse;
import com.quickbite.entity.*;
import com.quickbite.exception.BadRequestException;
import com.quickbite.exception.ResourceNotFoundException;
import com.quickbite.exception.UnauthorizedException;
import com.quickbite.repository.DeliveryRepository;
import com.quickbite.repository.OrderRepository;
import com.quickbite.repository.UserRepository;
import com.quickbite.service.DeliveryService;
import com.quickbite.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DeliveryResponse assignAgent(Long orderId, DeliveryAssignRequest request, String requesterEmail) {
        Order order = getOrder(orderId);
        User requester = getUser(requesterEmail);

        boolean isOwner = order.getRestaurant().getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to assign delivery for this order");
        }

        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery agent not found: " + request.getAgentId()));
        if (agent.getRole() != Role.DELIVERY_AGENT) {
            throw new BadRequestException("Selected user is not a delivery agent");
        }

        Delivery delivery = deliveryRepository.findByOrder(order).orElseGet(() ->
                Delivery.builder().order(order).status(DeliveryStatus.UNASSIGNED).build());

        delivery.setDeliveryAgent(agent);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        delivery = deliveryRepository.save(delivery);

        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);

        notificationService.notify(agent,
                "You have been assigned to deliver order #" + order.getId(),
                NotificationType.DELIVERY_ASSIGNED);
        notificationService.notify(order.getCustomer(),
                "A delivery agent has been assigned to your order #" + order.getId(),
                NotificationType.DELIVERY_ASSIGNED);

        log.info("Delivery agent {} assigned to order {} by {}", agent.getEmail(), orderId, requesterEmail);
        return toResponse(delivery);
    }

    @Override
    @Transactional
    public DeliveryResponse updateStatus(Long orderId, DeliveryStatusUpdateRequest request, String agentEmail) {
        Order order = getOrder(orderId);
        User agent = getUser(agentEmail);

        Delivery delivery = deliveryRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery record for order: " + orderId));

        if (delivery.getDeliveryAgent() == null || !delivery.getDeliveryAgent().getId().equals(agent.getId())) {
            throw new UnauthorizedException("You are not assigned to this delivery");
        }

        delivery.setStatus(request.getStatus());
        if (request.getCurrentLocation() != null) {
            delivery.setCurrentLocation(request.getCurrentLocation());
        }

        switch (request.getStatus()) {
            case PICKED_UP -> {
                delivery.setPickedUpAt(LocalDateTime.now());
                order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            }
            case ON_THE_WAY -> order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            case DELIVERED -> {
                delivery.setDeliveredAt(LocalDateTime.now());
                order.setStatus(OrderStatus.DELIVERED);
                notificationService.notify(order.getCustomer(),
                        "Your order #" + order.getId() + " has been delivered. Enjoy your meal!",
                        NotificationType.ORDER_DELIVERED);
            }
            case FAILED -> order.setStatus(OrderStatus.CANCELLED);
            default -> { /* no-op for UNASSIGNED/ASSIGNED */ }
        }

        orderRepository.save(order);
        delivery = deliveryRepository.save(delivery);

        log.info("Delivery status for order {} updated to {} by {}", orderId, request.getStatus(), agentEmail);
        return toResponse(delivery);
    }

    @Override
    public DeliveryResponse track(Long orderId, String requesterEmail) {
        Order order = getOrder(orderId);
        User requester = getUser(requesterEmail);

        boolean isCustomer = order.getCustomer().getId().equals(requester.getId());
        boolean isOwner = order.getRestaurant().getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isCustomer && !isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to track this order");
        }

        Delivery delivery = deliveryRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery record for order: " + orderId));
        return toResponse(delivery);
    }

    @Override
    public List<DeliveryResponse> getMyDeliveries(String agentEmail) {
        User agent = getUser(agentEmail);
        return deliveryRepository.findByDeliveryAgent(agent).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private DeliveryResponse toResponse(Delivery d) {
        return DeliveryResponse.builder()
                .id(d.getId())
                .orderId(d.getOrder().getId())
                .agentId(d.getDeliveryAgent() != null ? d.getDeliveryAgent().getId() : null)
                .agentName(d.getDeliveryAgent() != null ? d.getDeliveryAgent().getName() : null)
                .status(d.getStatus())
                .currentLocation(d.getCurrentLocation())
                .assignedAt(d.getAssignedAt())
                .pickedUpAt(d.getPickedUpAt())
                .deliveredAt(d.getDeliveredAt())
                .build();
    }
}
