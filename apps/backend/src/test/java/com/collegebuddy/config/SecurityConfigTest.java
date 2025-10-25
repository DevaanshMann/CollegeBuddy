package com.collegebuddy.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {
    @Autowired PasswordEncoder encoder;

    @Test
    void passwordEncoder_exists_andEncodes() {
        assertThat(encoder).isNotNull();
        assertThat(encoder.matches("p", encoder.encode("p"))).isTrue();
    }
}
