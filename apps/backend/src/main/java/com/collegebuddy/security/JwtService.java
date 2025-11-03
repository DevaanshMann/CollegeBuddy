package com.collegebuddy.security;

import org.springframework.stereotype.Service;

/*
    Issues and validates JWT tokens, including campus domain claims.
 */
@Service
public class JwtService {

    public String issueToken(Long userId, String campusDomain){
//        TODO: build and sign JWT
        return "stub.jwt.token";
    }

    public boolean validateToken(String token){
//        TODO: parse/verify signature/sxpiration
        return true;
    }

    public String extractCampusdomain(String token){
//        TODO: parse claim
        return "example.edu";
    }

    public Long extractUserId(String token){
//        TODO: parse subject
        return 1L;
    }
}
