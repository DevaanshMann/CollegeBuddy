package com.collegebuddy.controller;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import com.collegebuddy.exception.CredentialsInvalidException;
import com.collegebuddy.exception.DuplicateEmailException;
import com.collegebuddy.exception.InvalidEmailException;
import com.collegebuddy.repo.SchoolRepository;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.security.JwtService;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    // ---------- SIGNUP ----------
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> signup(@RequestBody SignupReq r) {
        if (r == null) {
            throw new InvalidEmailException("email and password required");
        }

        String email = safeLowerTrim(r.email());
        String password = safeTrim(r.password());

        if (email.isEmpty() || password.isEmpty()) {
            throw new InvalidEmailException("email and password required");
        }
        if (email.length() > MAX_LEN || password.length() > MAX_LEN) {
            throw new InvalidEmailException("email/password too long");
        }
        // strict email rules (exactly one '@', non-empty local/domain)
        if (!isValidEduEmail(email)) {
            throw new InvalidEmailException("Invalid email format");
        }
        // keep explicit .edu message (your tests assert this)
        if (!email.endsWith(".edu")) {
            throw new InvalidEmailException("Email must be .edu");
        }
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already in use");
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
        u.setPasswordHash(encoder.encode(password));
        u.setSchool(school);
        // Stubbed for dev; in prod you'd send a verification email first
        u.setEmailVerified(true);

        users.save(u);
        return Map.of("status", "ok");
    }

    // ---------- LOGIN ----------
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> login(@RequestBody LoginReq r) {
        if (r == null) {
            // tests expect 400 when body missing/blank
            throw new InvalidEmailException("email and password required");
        }

        String email = safeLowerTrim(r.email());
        String password = safeTrim(r.password());

        if (email.isEmpty() || password.isEmpty()) {
            // 400 for missing fields (not 401)
            throw new InvalidEmailException("email and password required");
        }
        if (email.length() > MAX_LEN || password.length() > MAX_LEN) {
            throw new InvalidEmailException("email/password too long");
        }

        var u = users.findByEmail(email)
                .orElseThrow(() -> new CredentialsInvalidException("Invalid credentials"));

        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new CredentialsInvalidException("Invalid credentials");
        }

        String access = jwt.issueAccess(u.getId(), u.getSchool().getId());
        return Map.of("accessToken", access);
    }

    // ---------- helpers ----------
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeLowerTrim(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    // exactly one '@', non-empty local and domain
    private static boolean isValidEduEmail(String email) {
        int first = email.indexOf('@');
        int last = email.lastIndexOf('@');
        if (first <= 0 || first != last || first == email.length() - 1) return false;
        String domain = email.substring(first + 1);
        return !domain.isBlank();
    }
}
