package com.collegebuddy.dto;

import java.util.List;

public record ConnectionStatusDto(
        List<UserDto> connections,
        List<UserDto> incomingRequests,
        List<UserDto> outgoingRequests
) {}
