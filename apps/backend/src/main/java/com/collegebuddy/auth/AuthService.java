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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository users;
    private final VerificationTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final com.collegebuddy.security.JwtService jwtService;

    public AuthService(UserRepository users,
                       VerificationTokenRepository tokens,
                       PasswordEncoder encoder,
                       TokenService tokenService,
                       EmailService emailService,
                       com.collegebuddy.security.JwtService jwtService) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    /**
     * Create user with PENDING_VERIFICATION and send verification email.
     */
    public AuthResponse signup(SignupRequest request) {
        // 1. Validate domain / .edu requirement
        if (request.email() == null || !request.email().endsWith(".edu")) {
            throw new InvalidEmailDomainException("Campus-only: must use .edu email");
        }

        // (Optional) also match request.campusDomain() against allowed School domains

        // 2. Check duplicate
        if (users.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyInUseException("Email already registered");
        }

        // 3. Create user in PENDING_VERIFICATION
        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setHashedPassword(encoder.encode(request.password()));
        newUser.setCampusDomain(request.campusDomain());
        newUser.setStatus(AccountStatus.PENDING_VERIFICATION);
        newUser.setRole(Role.STUDENT);

        User saved = users.save(newUser);

        // 4. Create verification token
        String tokenValue = tokenService.generateVerificationToken(saved.getId());

        // 5. "Send" email (right now this can just log/print)
        // In prod, this would include a clickable link like /verify?token=abc
        emailService.sendVerificationEmail(
                saved.getEmail(),
                tokenValue
        );

        // We do NOT return JWT yet because account isn't active
        return new AuthResponse("pending", null);
    }

    /**
     * Mark account ACTIVE if token is valid.
     */
    public void verifyEmail(VerifyEmailRequest request) {
        boolean ok = tokenService.validateVerificationToken(request.token());
        if (!ok) {
            throw new InvalidVerificationTokenException("Invalid or expired token");
        }

        tokenService.markUserActive(request.token());
    }

    /**
     * Login and receive JWT.
     */
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

        String jwt = jwtService.issueToken(
                user.getId(),
                user.getCampusDomain()
        );

        return new AuthResponse("ok", jwt);
    }

    /**
     * Re-send verification email for accounts still pending.
     */
    public void resendVerification(ResendVerificationRequest request) {
        Optional<User> userOpt = users.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            // silent no-op or throw, your choice
            return;
        }

        User u = userOpt.get();
        if (u.getStatus() == AccountStatus.ACTIVE) {
            return; // already verified
        }

        String tokenValue = tokenService.generateVerificationToken(u.getId());
        emailService.sendVerificationEmail(
                u.getEmail(),
                tokenValue
        );
    }
}
