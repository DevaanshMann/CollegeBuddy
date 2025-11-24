package com.collegebuddy.dto;

import java.util.List;
import java.util.Map;

public record ConnectionStatusDto(
        List<UserDto> connections,
        List<ConnectionRequestDto> incomingRequests,
        List<ConnectionRequestDto> outgoingRequests,
        Map<Long, Long> unreadCounts
) {}
