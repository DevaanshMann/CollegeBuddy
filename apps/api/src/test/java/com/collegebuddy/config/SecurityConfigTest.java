package com.collegebuddy.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityConfigTest {

    @Autowired PasswordEncoder encoder;

    @Test
    void passwordEncoderBean_exists_andEncodes() {
        assertThat(encoder).isNotNull();
        String hash = encoder.encode("p");
        assertThat(encoder.matches("p", hash)).isTrue();
    }
}
