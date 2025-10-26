package com.collegebuddy.dto.common;

public record PageRequestDto(int page, int size) {
    public PageRequestDto {
        if (page < 0) page = 0;
        if (size <= 0 || size > 200) size = 20;
    }
}