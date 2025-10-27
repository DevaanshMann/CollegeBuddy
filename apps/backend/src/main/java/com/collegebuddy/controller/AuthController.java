package com.collegebuddy.controller;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import com.collegebuddy.repo.SchoolRepository;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * AuthController
 * - POST /auth/signup : requires .edu email; creates School by domain if missing; creates User
 * - POST /auth/login  : verifies password and returns { accessToken }
 *
 * Note: Email verification is stubbed (auto-verified in dev). Replace with real
 * verification flow when Mailer + tokens are wired.
 */
@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final UserRepository users;
    private final SchoolRepository schools;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users,
                          SchoolRepository schools,
                          PasswordEncoder encoder,
                          JwtService jwt) {
        this.users = users;
        this.schools = schools;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // --- DTOs ---
    public record SignupReq(String email, String password) {}
    public record LoginReq(String email, String password) {}

    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> signup(@RequestBody SignupReq r) {
        if (r == null || r.email() == null || r.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email and password required");
        }

        String email = r.email().toLowerCase().trim();
        if (!email.endsWith(".edu")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be .edu");
        }
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        String domain = email.substring(email.indexOf('@') + 1);
        School school = schools.findByDomain(domain).orElseGet(() -> {
            School s = new School();
            s.setDomain(domain);
            s.setName(domain);
            return schools.save(s);
        });

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setSchool(school);
        // For now, consider verified in dev. Replace with real email verification.
        u.setEmailVerified(true);

        users.save(u);

        return Map.of("status", "ok");
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> login(@RequestBody LoginReq r) {
        if (r == null || r.email() == null || r.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email and password required");
        }

        var u = users.findByEmail(r.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(r.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String access = jwt.issueAccess(u.getId(), u.getSchool().getId());
        return Map.of("accessToken", access);
    }
}