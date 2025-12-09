package com.collegebuddy.search;

import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.dto.SearchRequest;
import com.collegebuddy.dto.SearchResultDto;
import com.collegebuddy.dto.UserDto;
import com.collegebuddy.dto.UserDtoMapper;
import com.collegebuddy.repo.BlockedUserRepository;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final UserDtoMapper userDtoMapper;
    private final BlockedUserRepository blockedUsers;

    public SearchService(UserRepository users,
                         ProfileRepository profiles,
                         UserDtoMapper userDtoMapper,
                         BlockedUserRepository blockedUsers) {
        this.users = users;
        this.profiles = profiles;
        this.userDtoMapper = userDtoMapper;
        this.blockedUsers = blockedUsers;
    }

    public SearchResultDto searchCampusDirectory(String campusDomain, Long requesterId, SearchRequest request) {
        String query = request.query() != null ? request.query().toLowerCase(Locale.ROOT) : "";

        // 1. only ACTIVE users from same campus
        List<User> campusUsers = users.findByCampusDomainAndStatus(campusDomain, AccountStatus.ACTIVE);

        // 2. load all profiles for those users
        List<Long> userIds = campusUsers.stream().map(User::getId).toList();
        List<Profile> campusProfiles = profiles.findAllById(userIds);

        // index profiles by userId
        var profilesByUserId = campusProfiles.stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        // 3. build UserDto list with visibility + query filter + block filter
        List<UserDto> results = userDtoMapper.toDtoList(campusUsers, profilesByUserId)
                .stream()
                .filter(dto -> {
                    // Only exclude users who have blocked the requester
                    // Allow users that the requester has blocked (they'll see them with "Blocked" tag in UI)
                    if (blockedUsers.existsByBlockerIdAndBlockedId(dto.id(), requesterId)) {
                        return false;
                    }

                    // visibility rules – PRIVATE profiles only visible to owner
                    if ("PRIVATE".equalsIgnoreCase(dto.profileVisibility()) && !dto.id().equals(requesterId)) {
                        return false;
                    }

                    // query filter – simple contains on displayName or campusDomain
                    if (query.isBlank()) return true;
                    String name = dto.displayName() != null ? dto.displayName().toLowerCase(Locale.ROOT) : "";
                    return name.contains(query) || dto.campusDomain().toLowerCase(Locale.ROOT).contains(query);
                })
                .toList();

        return new SearchResultDto(results);
    }
}
