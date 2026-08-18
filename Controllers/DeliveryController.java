package com.quickbite.controller;

import com.quickbite.dto.request.DeliveryAssignRequest;
import com.quickbite.dto.request.DeliveryStatusUpdateRequest;
import com.quickbite.dto.response.DeliveryResponse;
import com.quickbite.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Delivery", description = "Delivery assignment and order tracking")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/order/{orderId}/assign")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @Operation(summary = "Assign a delivery agent to an order")
    public ResponseEntity<DeliveryResponse> assign(@PathVariable Long orderId,
                                                     @Valid @RequestBody DeliveryAssignRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(deliveryService.assignAgent(orderId, request, auth.getName()));
    }

    @PatchMapping("/order/{orderId}/status")
    @PreAuthorize("hasRole('DELIVERY_AGENT')")
    @Operation(summary = "Update delivery status (assigned agent only)")
    public ResponseEntity<DeliveryResponse> updateStatus(@PathVariable Long orderId,
                                                           @Valid @RequestBody DeliveryStatusUpdateRequest request,
                                                           Authentication auth) {
        return ResponseEntity.ok(deliveryService.updateStatus(orderId, request, auth.getName()));
    }

    @GetMapping("/order/{orderId}/track")
    @Operation(summary = "Track the delivery status of an order")
    public ResponseEntity<DeliveryResponse> track(@PathVariable Long orderId, Authentication auth) {
        return ResponseEntity.ok(deliveryService.track(orderId, auth.getName()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('DELIVERY_AGENT')")
    @Operation(summary = "List deliveries assigned to the current delivery agent")
    public ResponseEntity<List<DeliveryResponse>> myDeliveries(Authentication auth) {
        return ResponseEntity.ok(deliveryService.getMyDeliveries(auth.getName()));
    }
}
