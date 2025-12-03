package com.collegebuddy.auth;

import com.collegebuddy.common.exceptions.EmailAlreadyInUseException;
import com.collegebuddy.common.exceptions.InvalidEmailDomainException;
import com.collegebuddy.common.exceptions.InvalidVerificationTokenException;
import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.domain.*;
import com.collegebuddy.dto.*;
import com.collegebuddy.email.EmailService;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.repo.VerificationTokenRepository;
import com.collegebuddy.repo.ProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository users;
    private final VerificationTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final com.collegebuddy.security.JwtService jwtService;
    private final ProfileRepository profiles;

    public AuthService(UserRepository users,
                       VerificationTokenRepository tokens,
                       PasswordEncoder encoder,
                       TokenService tokenService,
                       EmailService emailService,
                       com.collegebuddy.security.JwtService jwtService,
                       ProfileRepository profiles) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.profiles = profiles;
    }

    public AuthResponse signup(SignupRequest request) {
        if (request.email() == null || !request.email().endsWith(".edu")) {
            throw new InvalidEmailDomainException("Campus-only: must use .edu email");
        }

        if (users.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyInUseException("Email already registered");
        }

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setHashedPassword(encoder.encode(request.password()));
        newUser.setCampusDomain(request.campusDomain());
        newUser.setStatus(AccountStatus.PENDING_VERIFICATION);
        newUser.setRole(Role.STUDENT);

        User saved = users.save(newUser);

        String tokenValue = tokenService.generateVerificationToken(saved.getId());

        emailService.sendVerificationEmail(
                saved.getEmail(),
                tokenValue
        );

        return new AuthResponse("pending", null);
    }

    public void verifyEmail(VerifyEmailRequest request) {
        boolean ok = tokenService.validateVerificationToken(request.token());
        if (!ok) {
            throw new InvalidVerificationTokenException("Invalid or expired token");
        }

        tokenService.markUserActive(request.token());
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = users.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userOpt.get();

        if (!encoder.matches(request.password(), user.getHashedPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("Account not verified/active");
        }

        // Get profile to include displayName in JWT
        Profile profile = profiles.findById(user.getId()).orElse(null);
        String displayName = (profile != null) ? profile.getDisplayName() : user.getEmail().split("@")[0];

        String jwt = jwtService.issueToken(
                user.getId(),
                user.getCampusDomain(),
                user.getRole().name(),
                user.getEmail(),
                displayName
        );

        return new AuthResponse("ok", jwt);
    }

    public void resendVerification(ResendVerificationRequest request) {
        Optional<User> userOpt = users.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            return;
        }

        User u = userOpt.get();
        if (u.getStatus() == AccountStatus.ACTIVE) {
            return;
        }

        String tokenValue = tokenService.generateVerificationToken(u.getId());
        emailService.sendVerificationEmail(
                u.getEmail(),
                tokenValue
        );
    }
}
