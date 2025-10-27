package com.collegebuddy.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CampusGuardTest {

    private final CampusGuard guard = new CampusGuard();

    @Test
    void sameIds_allows() {
        assertDoesNotThrow(() -> guard.assertSameCampus(7L, 7L));
    }

    @Test
    void bothNull_allows() {
        assertDoesNotThrow(() -> guard.assertSameCampus(null, null));
    }

    @Test
    void differentIds_forbidden() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> guard.assertSameCampus(7L, 8L));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Campus-only access");
    }

    @Test
    void requesterNullOnly_forbidden() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> guard.assertSameCampus(null, 5L));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Campus-only access");
    }

    @Test
    void resourceNullOnly_forbidden() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> guard.assertSameCampus(5L, null));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Campus-only access");
    }

    @Test
    void sameIds_zero_allows() {
        assertDoesNotThrow(() -> guard.assertSameCampus(0L, 0L));
    }

    @Test
    void differentIds_minVsMax_forbidden() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> guard.assertSameCampus(Long.MIN_VALUE, Long.MAX_VALUE));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Campus-only access");
    }
}
