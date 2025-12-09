package com.collegebuddy.integration;

import com.collegebuddy.domain.Profile;
import com.collegebuddy.domain.User;
import com.collegebuddy.domain.Visibility;
import com.collegebuddy.groups.CreateGroupRequest;
import com.collegebuddy.groups.SendGroupMessageRequest;
import com.collegebuddy.testutil.BaseIntegrationTest;
import com.collegebuddy.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for group functionality.
 * Tests group creation, membership, messaging, and permissions.
 */
@DisplayName("Group Controller Integration Tests")
class GroupControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired(required = false)
    private com.collegebuddy.repo.GroupRepository groupRepository;

    @Autowired(required = false)
    private com.collegebuddy.repo.GroupMemberRepository groupMemberRepository;

    @Autowired(required = false)
    private com.collegebuddy.repo.GroupMessageRepository groupMessageRepository;

    private User alice;
    private User bob;
    private User charlie;
    private User differentCampusUser;
    private String aliceToken;
    private String bobToken;
    private String charlieToken;
    private String differentCampusToken;

    @BeforeEach
    void setupUsers() {
        alice = TestDataFactory.createActiveUser("alice@university.edu", "university.edu");
        alice = userRepository.save(alice);
        Profile aliceProfile = TestDataFactory.createProfile(alice.getId(), "Alice");
        profileRepository.save(aliceProfile);

        bob = TestDataFactory.createActiveUser("bob@university.edu", "university.edu");
        bob = userRepository.save(bob);
        Profile bobProfile = TestDataFactory.createProfile(bob.getId(), "Bob");
        profileRepository.save(bobProfile);

        charlie = TestDataFactory.createActiveUser("charlie@university.edu", "university.edu");
        charlie = userRepository.save(charlie);
        Profile charlieProfile = TestDataFactory.createProfile(charlie.getId(), "Charlie");
        profileRepository.save(charlieProfile);

        differentCampusUser = TestDataFactory.createActiveUser("dave@othercampus.edu", "othercampus.edu");
        differentCampusUser = userRepository.save(differentCampusUser);
        Profile daveProfile = TestDataFactory.createProfile(differentCampusUser.getId(), "Dave");
        profileRepository.save(daveProfile);

        aliceToken = generateToken(alice.getId(), alice.getCampusDomain());
        bobToken = generateToken(bob.getId(), bob.getCampusDomain());
        charlieToken = generateToken(charlie.getId(), charlie.getCampusDomain());
        differentCampusToken = generateToken(differentCampusUser.getId(), differentCampusUser.getCampusDomain());
    }

    @BeforeEach
    void cleanGroupData() {
        if (groupMessageRepository != null) groupMessageRepository.deleteAll();
        if (groupMemberRepository != null) groupMemberRepository.deleteAll();
        if (groupRepository != null) groupRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /groups")
    class CreateGroupTests {

        @Test
        @DisplayName("should successfully create a group")
        void createGroup_validRequest_shouldSucceed() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest(
                    "Study Group",
                    "CS5800 Study Group",
                    Visibility.PUBLIC
            );

            mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Study Group"))
                    .andExpect(jsonPath("$.description").value("CS5800 Study Group"))
                    .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                    .andExpect(jsonPath("$.creatorId").value(alice.getId()))
                    .andExpect(jsonPath("$.campusDomain").value("university.edu"))
                    .andExpect(jsonPath("$.memberCount").value(1)); // Creator is auto-member
        }

        @Test
        @DisplayName("should fail to create group without authentication")
        void createGroup_noAuth_shouldFail() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest(
                    "Study Group",
                    "Description",
                    Visibility.PUBLIC
            );

            mockMvc.perform(post("/groups")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should create group with campus domain of creator")
        void createGroup_shouldUseCampusDomain() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest(
                    "Campus Group",
                    "Only for this campus",
                    Visibility.PUBLIC
            );

            mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.campusDomain").value("university.edu"));
        }
    }

    @Nested
    @DisplayName("GET /groups")
    class ListGroupsTests {

        @Test
        @DisplayName("should return groups for user's campus with pagination")
        void getGroups_sameCampus_shouldReturnGroups() throws Exception {
            // Given: Create groups on same campus
            for (int i = 1; i <= 3; i++) {
                CreateGroupRequest request = new CreateGroupRequest(
                        "Group " + i,
                        "Description " + i,
                        Visibility.PUBLIC
                );

                mockMvc.perform(post("/groups")
                                .header("Authorization", bearerToken(aliceToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());
            }

            // When: Bob (same campus) lists groups
            mockMvc.perform(get("/groups")
                            .header("Authorization", bearerToken(bobToken))
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements").value(3));
        }

        @Test
        @DisplayName("should not return groups from different campus")
        void getGroups_differentCampus_shouldNotReturnGroups() throws Exception {
            // Given: Alice creates group on university.edu
            CreateGroupRequest request = new CreateGroupRequest(
                    "University Group",
                    "For university.edu only",
                    Visibility.PUBLIC
            );

            mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // When: Dave (othercampus.edu) lists groups
            mockMvc.perform(get("/groups")
                            .header("Authorization", bearerToken(differentCampusToken))
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("should search groups by name")
        void getGroups_withSearch_shouldFilterResults() throws Exception {
            // Given: Create groups with different names
            CreateGroupRequest group1 = new CreateGroupRequest("Study Group", "For studying", Visibility.PUBLIC);
            CreateGroupRequest group2 = new CreateGroupRequest("Gaming Club", "For gaming", Visibility.PUBLIC);
            CreateGroupRequest group3 = new CreateGroupRequest("Study Session", "Another study group", Visibility.PUBLIC);

            mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(group1)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(group2)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(group3)))
                    .andExpect(status().isOk());

            // When: Search for "Study"
            mockMvc.perform(get("/groups")
                            .header("Authorization", bearerToken(bobToken))
                            .param("search", "Study")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("POST /groups/{groupId}/join")
    class JoinGroupTests {

        private Long groupId;

        @BeforeEach
        void createTestGroup() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest(
                    "Test Group",
                    "For testing join",
                    Visibility.PUBLIC
            );

            String response = mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            groupId = objectMapper.readTree(response).get("id").asLong();
        }

        @Test
        @DisplayName("should successfully join a group")
        void joinGroup_validGroup_shouldSucceed() throws Exception {
            // When: Bob joins Alice's group
            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Joined group successfully"));

            // Verify member count increased
            mockMvc.perform(get("/groups/" + groupId)
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberCount").value(2));
        }

        @Test
        @DisplayName("should fail to join group from different campus")
        void joinGroup_differentCampus_shouldFail() throws Exception {
            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(differentCampusToken)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should fail to join group twice")
        void joinGroup_alreadyMember_shouldFail() throws Exception {
            // Given: Bob already joined
            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk());

            // When: Bob tries to join again
            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /groups/{groupId}/leave")
    class LeaveGroupTests {

        private Long groupId;

        @BeforeEach
        void createAndJoinGroup() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest(
                    "Test Group",
                    "For testing leave",
                    Visibility.PUBLIC
            );

            String response = mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            groupId = objectMapper.readTree(response).get("id").asLong();

            // Bob joins
            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should successfully leave a group")
        void leaveGroup_member_shouldSucceed() throws Exception {
            mockMvc.perform(post("/groups/" + groupId + "/leave")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Left group successfully"));
        }

        @Test
        @DisplayName("creator should not be able to leave their own group")
        void leaveGroup_creator_shouldFail() throws Exception {
            mockMvc.perform(post("/groups/" + groupId + "/leave")
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should fail to leave group not a member of")
        void leaveGroup_notMember_shouldFail() throws Exception {
            mockMvc.perform(post("/groups/" + groupId + "/leave")
                            .header("Authorization", bearerToken(charlieToken)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Group Messaging")
    class GroupMessagingTests {

        private Long groupId;

        @BeforeEach
        void createGroupAndAddMembers() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest(
                    "Chat Group",
                    "For testing messages",
                    Visibility.PUBLIC
            );

            String response = mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            groupId = objectMapper.readTree(response).get("id").asLong();

            // Bob joins
            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should send message to group")
        void sendGroupMessage_member_shouldSucceed() throws Exception {
            SendGroupMessageRequest request = new SendGroupMessageRequest("Hello everyone!");

            mockMvc.perform(post("/groups/" + groupId + "/messages")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.body").value("Hello everyone!"))
                    .andExpect(jsonPath("$.senderName").value("Alice"));
        }

        @Test
        @DisplayName("should fail to send message as non-member")
        void sendGroupMessage_nonMember_shouldFail() throws Exception {
            SendGroupMessageRequest request = new SendGroupMessageRequest("Unauthorized message");

            mockMvc.perform(post("/groups/" + groupId + "/messages")
                            .header("Authorization", bearerToken(charlieToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should get group messages as member")
        void getGroupMessages_member_shouldSucceed() throws Exception {
            // Given: Alice sends 2 messages
            SendGroupMessageRequest msg1 = new SendGroupMessageRequest("Message 1");
            SendGroupMessageRequest msg2 = new SendGroupMessageRequest("Message 2");

            mockMvc.perform(post("/groups/" + groupId + "/messages")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(msg1)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/groups/" + groupId + "/messages")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(msg2)))
                    .andExpect(status().isOk());

            // When: Bob retrieves messages
            mockMvc.perform(get("/groups/" + groupId + "/messages")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("should fail to get messages as non-member")
        void getGroupMessages_nonMember_shouldFail() throws Exception {
            mockMvc.perform(get("/groups/" + groupId + "/messages")
                            .header("Authorization", bearerToken(charlieToken)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should mark group messages as read")
        void markGroupAsRead_member_shouldSucceed() throws Exception {
            mockMvc.perform(post("/groups/" + groupId + "/mark-read")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should get unread counts for user's groups")
        void getUnreadCounts_shouldReturnCounts() throws Exception {
            mockMvc.perform(get("/groups/unread-counts")
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());
        }
    }

    @Nested
    @DisplayName("GET /groups/{groupId}/members")
    class GetMembersTests {

        private Long groupId;

        @BeforeEach
        void createGroupWithMembers() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest(
                    "Members Group",
                    "For testing members",
                    Visibility.PUBLIC
            );

            String response = mockMvc.perform(post("/groups")
                            .header("Authorization", bearerToken(aliceToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            groupId = objectMapper.readTree(response).get("id").asLong();

            // Bob and Charlie join
            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(bobToken)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/groups/" + groupId + "/join")
                            .header("Authorization", bearerToken(charlieToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return all group members")
        void getGroupMembers_member_shouldReturnAllMembers() throws Exception {
            mockMvc.perform(get("/groups/" + groupId + "/members")
                            .header("Authorization", bearerToken(aliceToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(3))); // Alice, Bob, Charlie
        }

        @Test
        @DisplayName("SECURITY NOTE: API currently allows cross-campus member viewing")
        void getGroupMembers_differentCampus_documentsCurrentBehavior() throws Exception {
            // CURRENT BEHAVIOR: API returns 200 (allows cross-campus users to view members)
            // DESIGN DECISION NEEDED: Is this intentional (open networking) or a security gap?
            // To restrict: Update GroupService.getGroupMembers() to verify campus domain match
            mockMvc.perform(get("/groups/" + groupId + "/members")
                            .header("Authorization", bearerToken(differentCampusToken)))
                    .andExpect(status().isOk()); // Documenting actual behavior
        }
    }
}
