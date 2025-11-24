package com.collegebuddy.integration;

import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.dto.SearchRequest;
import com.collegebuddy.testutil.BaseIntegrationTest;
import com.collegebuddy.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Search Controller Integration Tests")
class SearchControllerIntegrationTest extends BaseIntegrationTest {

    private User searcher;
    private User alice;
    private User bob;
    private User charlie; // Different campus
    private String searcherToken;

    @BeforeEach
    void setupUsers() {
        // Create searcher
        searcher = TestDataFactory.createActiveUser("searcher@university.edu", "university.edu");
        searcher = userRepository.save(searcher);
        profileRepository.save(TestDataFactory.createProfile(searcher.getId(), "Searcher"));

        // Create searchable users on same campus
        alice = TestDataFactory.createActiveUser("alice@university.edu", "university.edu");
        alice = userRepository.save(alice);
        profileRepository.save(TestDataFactory.createProfile(alice.getId(), "Alice Smith"));

        bob = TestDataFactory.createActiveUser("bob@university.edu", "university.edu");
        bob = userRepository.save(bob);
        profileRepository.save(TestDataFactory.createProfile(bob.getId(), "Bob Jones"));

        // Create user on different campus
        charlie = TestDataFactory.createActiveUser("charlie@othercampus.edu", "othercampus.edu");
        charlie = userRepository.save(charlie);
        profileRepository.save(TestDataFactory.createProfile(charlie.getId(), "Charlie Brown"));

        // Generate token
        searcherToken = generateToken(searcher.getId(), searcher.getCampusDomain());
    }

    @Nested
    @DisplayName("POST /search")
    class SearchTests {

        @Test
        @DisplayName("should find users by display name on same campus")
        void search_byName_shouldFindMatches() throws Exception {
            SearchRequest request = new SearchRequest("Alice");

            mockMvc.perform(post("/search")
                            .header("Authorization", bearerToken(searcherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results").isArray())
                    .andExpect(jsonPath("$.results[*].displayName", hasItem("Alice Smith")));
        }

        @Test
        @DisplayName("should not find users from different campus")
        void search_differentCampus_shouldNotFind() throws Exception {
            SearchRequest request = new SearchRequest("Charlie");

            mockMvc.perform(post("/search")
                            .header("Authorization", bearerToken(searcherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results").isArray())
                    .andExpect(jsonPath("$.results[*].displayName", not(hasItem("Charlie Brown"))));
        }

        @Test
        @DisplayName("should return empty results for no matches")
        void search_noMatches_shouldReturnEmpty() throws Exception {
            SearchRequest request = new SearchRequest("XYZ123NonExistent");

            mockMvc.perform(post("/search")
                            .header("Authorization", bearerToken(searcherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results").isArray())
                    .andExpect(jsonPath("$.results", hasSize(0)));
        }

        @Test
        @DisplayName("should include searcher in results when searching own name")
        void search_shouldIncludeSelfWhenMatching() throws Exception {
            SearchRequest request = new SearchRequest("Searcher");

            mockMvc.perform(post("/search")
                            .header("Authorization", bearerToken(searcherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results").isArray())
                    // Users can see themselves in search results
                    .andExpect(jsonPath("$.results[?(@.displayName == 'Searcher')]").isNotEmpty());
        }

        @Test
        @DisplayName("should find multiple users matching query")
        void search_multipleMatches_shouldFindAll() throws Exception {
            // Create another user with "Smith" in name
            User john = TestDataFactory.createActiveUser("john@university.edu", "university.edu");
            john = userRepository.save(john);
            profileRepository.save(TestDataFactory.createProfile(john.getId(), "John Smith"));

            SearchRequest request = new SearchRequest("Smith");

            mockMvc.perform(post("/search")
                            .header("Authorization", bearerToken(searcherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results").isArray())
                    .andExpect(jsonPath("$.results", hasSize(greaterThanOrEqualTo(2))));
        }

        @Test
        @DisplayName("should fail without authentication")
        void search_noAuth_shouldFail() throws Exception {
            SearchRequest request = new SearchRequest("Alice");

            mockMvc.perform(post("/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should handle case-insensitive search")
        void search_caseInsensitive_shouldFind() throws Exception {
            SearchRequest request = new SearchRequest("alice");

            mockMvc.perform(post("/search")
                            .header("Authorization", bearerToken(searcherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results").isArray())
                    .andExpect(jsonPath("$.results[*].displayName", hasItem("Alice Smith")));
        }

        @Test
        @DisplayName("should not expose private profiles in search")
        void search_privateProfile_shouldNotShow() throws Exception {
            // Create a private user
            User privateUser = TestDataFactory.createActiveUser("private@university.edu", "university.edu");
            privateUser = userRepository.save(privateUser);
            profileRepository.save(TestDataFactory.createProfile(privateUser.getId(), "Private Person", Visibility.PRIVATE));

            SearchRequest request = new SearchRequest("Private");

            mockMvc.perform(post("/search")
                            .header("Authorization", bearerToken(searcherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results[*].displayName", not(hasItem("Private Person"))));
        }
    }
}
