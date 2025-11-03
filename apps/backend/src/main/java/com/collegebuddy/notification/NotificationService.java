package com.collegebuddy.notification;

import org.springframework.stereotype.Service;

/**
 * In-app (and maybe email) notifications:
 * - "X sent you a request"
 * - "Your request was accepted"
 */
@Service
public class NotificationService {

    public void notifyUser(Long userId, String message) {
        // TODO: persist or dispatch notification
    }
}
