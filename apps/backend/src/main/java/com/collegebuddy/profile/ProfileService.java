package com.collegebuddy.profile;

import com.collegebuddy.dto.ProfileResponse;
import com.collegebuddy.dto.ProfileUpdateRequest;
import org.springframework.stereotype.Service;

/*
    Profile persistence and visibility enforcement
 */

@Service
public class ProfileService {

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request){
//        TODO: persist profile data
        return new ProfileResponse("name", "bio", "avatarUrl", request.visibility());
    }

    public ProfileResponse getProfile(Long userId, Long requestId){
//        TODO: visibility logic
        return new ProfileResponse("name", "bio", "avatarUrl", "PUBLIC");
    }
}
