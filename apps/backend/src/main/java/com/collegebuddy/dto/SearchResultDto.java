package com.collegebuddy.dto;

import java.util.List;

public record SearchResultDto(
        List<UserDto> results
) {
    public SearchResultDto() {
        this(List.of());
    }
}
