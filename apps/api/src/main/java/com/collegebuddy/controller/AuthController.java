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

@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private static final int MAX_LEN = 255;

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
        if (r == null) badRequest("email and password required");

        String email = safeLowerTrim(r.email());
        String password = safeTrim(r.password());

        // basic presence
        if (email.isEmpty() || password.isEmpty()) {
            badRequest("email and password required");
        }

        // simple format & length checks
        if (email.length() > MAX_LEN || password.length() > MAX_LEN) {
            badRequest("email/password too long");
        }
        if (!email.contains("@")) {
            badRequest("Invalid email format");
        }
        int at = email.indexOf('@');
        String domain = at >= 0 && at < email.length() - 1 ? email.substring(at + 1) : "";
        if (domain.isEmpty()) {
            badRequest("Invalid email domain");
        }
        if (!email.endsWith(".edu")) {
            badRequest("Email must be .edu");
        }

        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        // upsert school by domain
        School school = schools.findByDomain(domain).orElseGet(() -> {
            School s = new School();
            s.setDomain(domain);
            s.setName(domain);
            return schools.save(s);
        });

        // create user
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setSchool(school);
        u.setEmailVerified(true); // stubbed in dev

        users.save(u);
        return Map.of("status", "ok");
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> login(@RequestBody LoginReq r) {
        if (r == null) badRequest("email and password required");

        String email = safeLowerTrim(r.email());
        String password = safeTrim(r.password());

        if (email.isEmpty() || password.isEmpty()) {
            badRequest("email and password required");
        }
        if (email.length() > MAX_LEN || password.length() > MAX_LEN) {
            badRequest("email/password too long");
        }

        var u = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String access = jwt.issueAccess(u.getId(), u.getSchool().getId());
        return Map.of("accessToken", access);
    }

    // --- helpers ---
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeLowerTrim(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static void badRequest(String msg) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}
