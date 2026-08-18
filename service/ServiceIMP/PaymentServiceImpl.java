package com.quickbite.service.impl;

import com.quickbite.dto.request.PaymentRequest;
import com.quickbite.dto.response.PaymentResponse;
import com.quickbite.entity.*;
import com.quickbite.exception.BadRequestException;
import com.quickbite.exception.ResourceNotFoundException;
import com.quickbite.exception.UnauthorizedException;
import com.quickbite.repository.OrderRepository;
import com.quickbite.repository.PaymentRepository;
import com.quickbite.repository.UserRepository;
import com.quickbite.service.NotificationService;
import com.quickbite.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simulated payment gateway integration. In production this would call out to
 * a real provider (Stripe/Razorpay/etc.) instead of auto-approving the payment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PaymentResponse makePayment(String customerEmail, PaymentRequest request) {
        User customer = getUser(customerEmail);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("This order does not belong to you");
        }

        if (paymentRepository.findByOrder(order).isPresent()) {
            throw new BadRequestException("Payment already exists for order " + order.getId());
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(request.getMethod())
                .status(PaymentStatus.SUCCESS) // simulated gateway auto-approves
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        notificationService.notify(customer,
                "Payment of " + payment.getAmount() + " received for order #" + order.getId(),
                NotificationType.PAYMENT_SUCCESS);

        log.info("Payment {} recorded for order {} via {}", payment.getTransactionId(), order.getId(), request.getMethod());
        return toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrder(Long orderId, String requesterEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        User requester = getUser(requesterEmail);

        boolean isCustomer = order.getCustomer().getId().equals(requester.getId());
        boolean isOwner = order.getRestaurant().getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isCustomer && !isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view this payment");
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for order: " + orderId));
        return toResponse(payment);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .amount(p.getAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .transactionId(p.getTransactionId())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .build();
    }
}
