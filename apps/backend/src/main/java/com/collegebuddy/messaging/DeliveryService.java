package com.collegebuddy.messaging;

import org.springframework.stereotype.Service;

/**
 * Handles real-time delivery (WebSocket, push, etc.)
 */
@Service
public class DeliveryService {

    public void deliverToUser(Long userId, String messageBody) {
        // TODO: push event to WebSocket session or notification channel
    }
}
