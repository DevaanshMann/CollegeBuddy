package com.collegebuddy.dto.friends;

public record FriendshipStatusDto(
        Long userId,
        boolean areFriends,
        boolean requestPending,
        boolean incomingRequest
) {}