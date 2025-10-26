package com.collegebuddy.dto.friends;

public record FriendSuggestionDto(
        Long userId,
        String displayName,
        String school,
        int mutualFriends
) {}