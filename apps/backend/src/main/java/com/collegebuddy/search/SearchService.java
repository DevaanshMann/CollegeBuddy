package com.collegebuddy.search;

import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.dto.SearchRequest;
import com.collegebuddy.dto.SearchResultDto;
import com.collegebuddy.dto.UserDto;
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

    public SearchService(UserRepository users,
                         ProfileRepository profiles) {
        this.users = users;
        this.profiles = profiles;
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

        // 3. build UserDto list with visibility + query filter
        List<UserDto> results = campusUsers.stream()
                .map(u -> {
                    Profile p = profilesByUserId.get(u.getId());
                    String displayName = p != null ? p.getDisplayName() : u.getEmail();
                    String avatarUrl = p != null ? p.getAvatarUrl() : null;
                    String visibility = p != null ? p.getVisibility().name() : Visibility.PUBLIC.name();

                    return new UserDto(
                            u.getId(),
                            displayName,
                            avatarUrl,
                            visibility,
                            u.getCampusDomain()
                    );
                })
                .filter(dto -> {
                    // visibility rules – PRIVATE profiles only visible to owner
                    if ("PRIVATE".equalsIgnoreCase(dto.visibility()) && !dto.userId().equals(requesterId)) {
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
