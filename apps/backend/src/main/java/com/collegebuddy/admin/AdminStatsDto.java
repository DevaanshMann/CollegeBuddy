package com.collegebuddy.admin;

public record AdminStatsDto(
        long totalUsers,
        long activeUsers,
        long pendingVerificationUsers,
        long deactivatedUsers,
        long totalConnections,
        long totalMessages
) {
}
