package com.quickbite.service;

import com.quickbite.dto.request.PaymentRequest;
import com.quickbite.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse makePayment(String customerEmail, PaymentRequest request);
    PaymentResponse getPaymentByOrder(Long orderId, String requesterEmail);
}
