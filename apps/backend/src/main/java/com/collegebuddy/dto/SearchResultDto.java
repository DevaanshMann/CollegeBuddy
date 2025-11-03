package com.collegebuddy.dto;

import java.util.List;

/**
 * Represents a list of matching profiles.
 * You can expand this with campus info / preview restrictions.
 */
public record SearchResultDto(
        List<UserDto> results
) {
    public SearchResultDto() {
        this(List.of());
    }
}
