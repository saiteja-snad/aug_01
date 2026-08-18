package com.quickbite.controller;

import com.quickbite.dto.request.PaymentRequest;
import com.quickbite.dto.response.PaymentResponse;
import com.quickbite.service.PaymentService;
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

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payments", description = "Payment processing (simulated gateway)")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Pay for a placed order")
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.makePayment(auth.getName(), request));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment details for an order")
    public ResponseEntity<PaymentResponse> getByOrder(@PathVariable Long orderId, Authentication auth) {
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId, auth.getName()));
    }
}
