package com.collegebuddy.dto;

import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.Visibility;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting domain entities to UserDto.
 * Centralizes DTO mapping logic to eliminate duplication across services.
 *
 * Implements Factory Pattern for DTO creation.
 */
@Component
public class UserDtoMapper {

    /**
     * Maps a User and Profile to UserDto.
     *
     * @param user The user entity
     * @param profile The profile entity (may be null)
     * @return UserDto representation
     */
    public UserDto toDto(User user, Profile profile) {
        if (user == null) {
            return null;
        }

        String displayName = (profile != null && profile.getDisplayName() != null)
                ? profile.getDisplayName()
                : user.getEmail();

        String avatarUrl = (profile != null) ? profile.getAvatarUrl() : null;

        String visibility = (profile != null && profile.getVisibility() != null)
                ? profile.getVisibility().name()
                : Visibility.PUBLIC.name();

        return new UserDto(
                user.getId(),
                user.getEmail(),
                displayName,
                user.getCampusDomain(),
                avatarUrl,
                visibility,
                user.getRole().name()
        );
    }

    /**
     * Maps multiple Users to UserDtos using a profile map.
     * Efficient batch mapping when profiles are already loaded.
     *
     * @param users List of users to map
     * @param profilesByUserId Map of profiles indexed by user ID
     * @return List of UserDto objects
     */
    public List<UserDto> toDtoList(List<User> users, Map<Long, Profile> profilesByUserId) {
        return users.stream()
                .map(user -> toDto(user, profilesByUserId.get(user.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Maps a single User to UserDto, loading profile if needed.
     * Use this when you already have both entities.
     *
     * @param user The user entity
     * @return UserDto representation
     */
    public UserDto toDto(User user) {
        return toDto(user, null);
    }
}
