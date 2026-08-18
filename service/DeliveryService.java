package com.quickbite.service;

import com.quickbite.dto.request.DeliveryAssignRequest;
import com.quickbite.dto.request.DeliveryStatusUpdateRequest;
import com.quickbite.dto.response.DeliveryResponse;

import java.util.List;

public interface DeliveryService {
    DeliveryResponse assignAgent(Long orderId, DeliveryAssignRequest request, String requesterEmail);
    DeliveryResponse updateStatus(Long orderId, DeliveryStatusUpdateRequest request, String agentEmail);
    DeliveryResponse track(Long orderId, String requesterEmail);
    List<DeliveryResponse> getMyDeliveries(String agentEmail);
}
