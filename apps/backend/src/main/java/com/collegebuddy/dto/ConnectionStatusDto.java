package com.collegebuddy.dto;

import java.util.List;

public record ConnectionStatusDto(
        List<UserDto> connections,
        List<ConnectionRequestDto> incomingRequests,
        List<ConnectionRequestDto> outgoingRequests
) {}
