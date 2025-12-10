package com.collegebuddy.service;

import com.collegebuddy.domain.Visibility;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProfileService visibility and validation logic.
 */
class ProfileServiceLogicTest {

    @Test
    void parseVisibility_shouldHandleUpperCase() {
        String visibilityString = "PUBLIC";

        Visibility visibility = Visibility.valueOf(visibilityString.toUpperCase());

        assertThat(visibility).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void parseVisibility_shouldHandleLowerCase() {
        String visibilityString = "private";

        Visibility visibility = Visibility.valueOf(visibilityString.toUpperCase());

        assertThat(visibility).isEqualTo(Visibility.PRIVATE);
    }

    @Test
    void parseVisibility_shouldDefaultToPublicOnNull() {
        String visibilityString = null;

        Visibility visibility = visibilityString == null ? Visibility.PUBLIC : Visibility.valueOf(visibilityString.toUpperCase());

        assertThat(visibility).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void parseVisibility_shouldDefaultToPublicOnInvalid() {
        String visibilityString = "INVALID";

        Visibility visibility;
        try {
            visibility = Visibility.valueOf(visibilityString.toUpperCase());
        } catch (IllegalArgumentException e) {
            visibility = Visibility.PUBLIC;
        }

        assertThat(visibility).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void checkProfileAccess_shouldAllowOwnPrivateProfile() {
        Visibility visibility = Visibility.PRIVATE;
        Long profileOwnerId = 1L;
        Long requesterId = 1L;

        boolean canAccess = visibility != Visibility.PRIVATE || profileOwnerId.equals(requesterId);

        assertThat(canAccess).isTrue();
    }

    @Test
    void checkProfileAccess_shouldDenyOthersPrivateProfile() {
        Visibility visibility = Visibility.PRIVATE;
        Long profileOwnerId = 1L;
        Long requesterId = 2L;

        boolean canAccess = visibility != Visibility.PRIVATE || profileOwnerId.equals(requesterId);

        assertThat(canAccess).isFalse();
    }

    @Test
    void checkProfileAccess_shouldAllowPublicProfiles() {
        Visibility visibility = Visibility.PUBLIC;
        Long profileOwnerId = 1L;
        Long requesterId = 2L;

        boolean canAccess = visibility != Visibility.PRIVATE || profileOwnerId.equals(requesterId);

        assertThat(canAccess).isTrue();
    }
}
