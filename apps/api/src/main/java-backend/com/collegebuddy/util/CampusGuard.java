package com.collegebuddy.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;

@Component

public class CampusGuard {
    public void assertSameCampus(Long requesterSchoolId, Long resourceSchoolId){
        if(!Objects.equals(requesterSchoolId, resourceSchoolId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Campus-only access");
        }
    }
}