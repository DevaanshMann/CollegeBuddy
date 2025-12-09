package com.collegebuddy.account;

import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * DELETE /account
     * Permanently delete the authenticated user's account and all associated data.
     * Requires password confirmation.
     */
    @DeleteMapping
    public ResponseEntity<?> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request
    ) {
        AuthenticatedUser auth = SecurityUtils.getCurrentUser();
        log.info("DELETE /account - User ID: {}", auth.id());

        accountService.deleteAccount(auth.id(), request.password());

        return ResponseEntity.ok(Map.of(
                "message", "Account deleted successfully"
        ));
    }
}
