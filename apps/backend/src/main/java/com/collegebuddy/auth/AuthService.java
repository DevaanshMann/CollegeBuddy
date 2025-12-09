package com.collegebuddy.auth;

import com.collegebuddy.common.exceptions.EmailAlreadyInUseException;
import com.collegebuddy.common.exceptions.InvalidEmailDomainException;
import com.collegebuddy.common.exceptions.InvalidVerificationTokenException;
import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.PasswordResetToken;
import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.Role;
import com.collegebuddy.domain.User;
import com.collegebuddy.dto.AuthResponse;
import com.collegebuddy.dto.ForgotPasswordRequest;
import com.collegebuddy.dto.LoginRequest;
import com.collegebuddy.dto.ResendVerificationRequest;
import com.collegebuddy.dto.ResetPasswordRequest;
import com.collegebuddy.dto.SignupRequest;
import com.collegebuddy.dto.UserDto;
import com.collegebuddy.dto.UserDtoMapper;
import com.collegebuddy.dto.VerifyEmailRequest;
import com.collegebuddy.email.EmailService;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.repo.VerificationTokenRepository;
import com.collegebuddy.repo.PasswordResetTokenRepository;
import com.collegebuddy.repo.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final VerificationTokenRepository tokens;
    private final PasswordResetTokenRepository passwordResetTokens;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final com.collegebuddy.security.JwtService jwtService;
    private final ProfileRepository profiles;
    private final UserDtoMapper userDtoMapper;

    public AuthService(UserRepository users,
                       VerificationTokenRepository tokens,
                       PasswordResetTokenRepository passwordResetTokens,
                       PasswordEncoder encoder,
                       TokenService tokenService,
                       EmailService emailService,
                       com.collegebuddy.security.JwtService jwtService,
                       ProfileRepository profiles,
                       UserDtoMapper userDtoMapper) {
        this.users = users;
        this.tokens = tokens;
        this.passwordResetTokens = passwordResetTokens;
        this.encoder = encoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.profiles = profiles;
        this.userDtoMapper = userDtoMapper;
    }

    public UserDto getUserById(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        Profile profile = profiles.findById(userId).orElse(null);
        return userDtoMapper.toDto(user, profile);
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

        // Try to send verification email, but don't fail signup if email fails
        try {
            emailService.sendVerificationEmail(
                    saved.getEmail(),
                    tokenValue
            );
            log.info("Verification email sent successfully to {}", saved.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}. User can request resend later. Error: {}",
                    saved.getEmail(), e.getMessage());
            // Don't throw - allow signup to succeed even if email fails
        }

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

        // Try to send verification email, but don't fail if email service is down
        try {
            emailService.sendVerificationEmail(
                    u.getEmail(),
                    tokenValue
            );
            log.info("Resent verification email to {}", u.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend verification email to {}. Error: {}",
                    u.getEmail(), e.getMessage());
            // Don't throw - just log the error
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = users.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not - security best practice
            return;
        }

        User user = userOpt.get();

        // Generate reset token (15 minutes expiration)
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(tokenValue);
        resetToken.setUserId(user.getId());
        resetToken.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        passwordResetTokens.save(resetToken);

        // Send password reset email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}. Error: {}",
                    user.getEmail(), e.getMessage());
            // Don't throw - just log the error
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokens.findByToken(request.token());
        if (tokenOpt.isEmpty()) {
            throw new InvalidVerificationTokenException("Invalid or expired reset token");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidVerificationTokenException("Invalid or expired reset token");
        }

        // Update user password
        Optional<User> userOpt = users.findById(resetToken.getUserId());
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("User not found");
        }

        User user = userOpt.get();
        user.setHashedPassword(encoder.encode(request.newPassword()));
        users.save(user);

        // Delete the used token
        passwordResetTokens.delete(resetToken);

        log.info("Password reset successfully for user {}", user.getEmail());
    }
}
