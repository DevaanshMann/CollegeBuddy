package com.collegebuddy.util;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;

class CampusGuardTest {
    CampusGuard guard = new CampusGuard();
    @Test void sameCampus_noException(){ guard.assertSameCampus(1L,1L); }
    @Test void differentCampus_throwsForbidden(){
        assertThatThrownBy(() -> guard.assertSameCampus(1L,2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Campus-only");
    }
}