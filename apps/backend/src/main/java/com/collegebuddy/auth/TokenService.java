package com.collegebuddy.auth;

import com.collegebuddy.domain.AccountStatus;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.VerificationToken;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.repo.VerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private final VerificationTokenRepository tokens;
    private final UserRepository users;

    public TokenService(VerificationTokenRepository tokens,
                        UserRepository users) {
        this.tokens = tokens;
        this.users = users;
    }

    public String generateVerificationToken(Long userId) {
        String value = UUID.randomUUID().toString();

        VerificationToken token = new VerificationToken();
        token.setToken(value);
        token.setUserId(userId);
        token.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));

        tokens.save(token);

        return value;
    }

    public boolean validateVerificationToken(String tokenValue) {
        Optional<VerificationToken> vtOpt = tokens.findByToken(tokenValue);
        if (vtOpt.isEmpty()) return false;

        VerificationToken vt = vtOpt.get();
        if (vt.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        return true;
    }

    public void markUserActive(String tokenValue) {
        Optional<VerificationToken> vtOpt = tokens.findByToken(tokenValue);
        if (vtOpt.isEmpty()) return;

        VerificationToken vt = vtOpt.get();
        if (vt.getExpiresAt().isBefore(Instant.now())) return;

        Long userId = vt.getUserId();

        users.findById(userId).ifPresent(u -> {
            u.setStatus(AccountStatus.ACTIVE);
            users.save(u);
        });

        // optional cleanup: delete token so it can't be reused
        tokens.delete(vt);
    }
}
