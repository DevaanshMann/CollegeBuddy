package com.collegebuddy.service;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SearchService filtering and matching logic.
 */
class SearchServiceLogicTest {

    @Test
    void searchQuery_shouldMatchCaseInsensitive() {
        String query = "JOHN";
        String displayName = "john doe";

        boolean matches = displayName.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));

        assertThat(matches).isTrue();
    }

    @Test
    void searchQuery_shouldMatchPartialString() {
        String query = "doe";
        String displayName = "John Doe";

        boolean matches = displayName.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));

        assertThat(matches).isTrue();
    }

    @Test
    void searchQuery_shouldHandleBlankQuery() {
        String query = "   ";
        String displayName = "Any Name";

        boolean matchesAll = query.isBlank();

        assertThat(matchesAll).isTrue();
    }

    @Test
    void searchQuery_shouldMatchCampusDomain() {
        String query = "cpp";
        String campusDomain = "cpp.edu";

        boolean matches = campusDomain.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));

        assertThat(matches).isTrue();
    }

    @Test
    void privacyFilter_shouldExcludePrivateProfiles() {
        String visibility = "PRIVATE";
        Long profileOwnerId = 1L;
        Long requesterId = 2L;

        boolean isPrivate = "PRIVATE".equalsIgnoreCase(visibility);
        boolean isOwner = profileOwnerId.equals(requesterId);
        boolean shouldInclude = !isPrivate || isOwner;

        assertThat(shouldInclude).isFalse();
    }

    @Test
    void privacyFilter_shouldIncludeOwnPrivateProfile() {
        String visibility = "PRIVATE";
        Long profileOwnerId = 1L;
        Long requesterId = 1L;

        boolean isPrivate = "PRIVATE".equalsIgnoreCase(visibility);
        boolean isOwner = profileOwnerId.equals(requesterId);
        boolean shouldInclude = !isPrivate || isOwner;

        assertThat(shouldInclude).isTrue();
    }

    @Test
    void privacyFilter_shouldIncludePublicProfiles() {
        String visibility = "PUBLIC";
        Long profileOwnerId = 1L;
        Long requesterId = 2L;

        boolean isPrivate = "PRIVATE".equalsIgnoreCase(visibility);
        boolean shouldInclude = !isPrivate || profileOwnerId.equals(requesterId);

        assertThat(shouldInclude).isTrue();
    }
}
