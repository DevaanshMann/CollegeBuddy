package com.collegebuddy.connection;

import com.collegebuddy.common.exceptions.ConnectionAlreadyExistsException;
import com.collegebuddy.common.exceptions.ConnectionNotFoundException;
import com.collegebuddy.common.exceptions.ConnectionRequestNotFoundException;
import com.collegebuddy.common.exceptions.ForbiddenCampusAccessException;
import com.collegebuddy.common.exceptions.InvalidConnectionActionException;
import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.domain.*;
import com.collegebuddy.dto.ConnectionRequestDto;
import com.collegebuddy.dto.ConnectionStatusDto;
import com.collegebuddy.dto.RespondToConnectionDto;
import com.collegebuddy.dto.SendConnectionRequestDto;
import com.collegebuddy.dto.UserDto;
import com.collegebuddy.dto.UserDtoMapper;
import com.collegebuddy.messaging.MessagingService;
import com.collegebuddy.repo.BlockedUserRepository;
import com.collegebuddy.repo.ConnectionRepository;
import com.collegebuddy.repo.ConnectionRequestRepository;
import com.collegebuddy.repo.ConversationRepository;
import com.collegebuddy.repo.MessageRepository;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectionService {

    private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);

    private final ConnectionRepository connections;
    private final ConnectionRequestRepository requests;
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final UserRepository users;
    private final ProfileRepository profiles;
    private final UserDtoMapper userDtoMapper;
    private final MessagingService messagingService;
    private final BlockedUserRepository blockedUsers;

    public ConnectionService(ConnectionRepository connections,
                             ConnectionRequestRepository requests,
                             ConversationRepository conversations,
                             MessageRepository messages,
                             UserRepository users,
                             ProfileRepository profiles,
                             UserDtoMapper userDtoMapper,
                             MessagingService messagingService,
                             BlockedUserRepository blockedUsers) {
        this.connections = connections;
        this.requests = requests;
        this.conversations = conversations;
        this.messages = messages;
        this.users = users;
        this.profiles = profiles;
        this.userDtoMapper = userDtoMapper;
        this.messagingService = messagingService;
        this.blockedUsers = blockedUsers;
    }

    @Transactional
    public void sendConnectionRequest(Long requesterId, String requesterCampusDomain, SendConnectionRequestDto dto) {
        log.info("sendConnectionRequest: requesterId={}, toUserId={}", requesterId, dto.toUserId());

        try {
            Long toUserId = dto.toUserId();

            if (Objects.equals(requesterId, toUserId)) {
                throw new InvalidConnectionActionException("Cannot connect to yourself");
            }

            log.info("Step 1: Finding target user {}", toUserId);
            User toUser = users.findById(toUserId)
                    .orElseThrow(() -> new InvalidConnectionActionException("Target user not found"));
            log.info("Step 1 complete: Found user {}", toUser.getEmail());

            if (!requesterCampusDomain.equalsIgnoreCase(toUser.getCampusDomain())) {
                throw new ForbiddenCampusAccessException("Cannot connect across campuses");
            }

            // Check if there's a block between users
            if (blockedUsers.existsBlockBetween(requesterId, toUserId)) {
                throw new InvalidConnectionActionException("Cannot send connection request to this user");
            }

            long a = Math.min(requesterId, toUserId);
            long b = Math.max(requesterId, toUserId);

            log.info("Step 2: Checking if connection exists between {} and {}", a, b);
            if (connections.existsByUserAIdAndUserBId(a, b)) {
                throw new ConnectionAlreadyExistsException("Users are already connected");
            }
            log.info("Step 2 complete: No existing connection");

            log.info("Step 3: Checking for pending requests");
            boolean pendingExists =
                    requests.existsByFromUserIdAndToUserIdAndStatus(requesterId, toUserId, ConnectionRequestStatus.PENDING) ||
                            requests.existsByFromUserIdAndToUserIdAndStatus(toUserId, requesterId, ConnectionRequestStatus.PENDING);

            if (pendingExists) {
                throw new InvalidConnectionActionException("There is already a pending request between these users");
            }
            log.info("Step 3 complete: No pending requests");

            log.info("Step 4: Cleaning up old connection requests");
            requests.deleteByFromUserIdAndToUserId(requesterId, toUserId);
            requests.deleteByFromUserIdAndToUserId(toUserId, requesterId);
            log.info("Step 4 complete: Old requests cleaned up");

            log.info("Step 5: Creating connection request");
            ConnectionRequest req = new ConnectionRequest();
            req.setFromUserId(requesterId);
            req.setToUserId(toUserId);
            req.setMessage(dto.message());
            req.setStatus(ConnectionRequestStatus.PENDING);
            req.setCreatedAt(Instant.now());

            requests.save(req);
            log.info("Step 5 complete: Connection request saved");
        } catch (Exception e) {
            log.error("Error in sendConnectionRequest: requesterId={}, toUserId={}", requesterId, dto.toUserId(), e);
            throw e;
        }
    }

    @Transactional
    public void respondToConnectionRequest(Long responderId, RespondToConnectionDto dto) {
        ConnectionRequest req = requests.findById(dto.requestId())
                .orElseThrow(() -> new ConnectionRequestNotFoundException("Request not found"));

        if (!Objects.equals(req.getToUserId(), responderId)) {
            throw new UnauthorizedException("You are not the recipient of this request");
        }

        if (req.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new InvalidConnectionActionException("Request is not pending");
        }

        String decision = dto.decision() != null ? dto.decision().toUpperCase() : "";

        if ("ACCEPT".equals(decision)) {
            req.setStatus(ConnectionRequestStatus.ACCEPTED);
            requests.save(req);

            long a = Math.min(req.getFromUserId(), req.getToUserId());
            long b = Math.max(req.getFromUserId(), req.getToUserId());

            if (!connections.existsByUserAIdAndUserBId(a, b)) {
                Connection conn = new Connection();
                conn.setUserAId(a);
                conn.setUserBId(b);
                conn.setCreatedAt(Instant.now());
                connections.save(conn);
            }

        } else if ("DECLINE".equals(decision)) {
            req.setStatus(ConnectionRequestStatus.DECLINED);
            requests.save(req);
        } else {
            throw new InvalidConnectionActionException("Decision must be ACCEPT or DECLINE");
        }
    }

    @Transactional
    public void disconnect(Long currentUserId, Long otherUserId) {
        if (Objects.equals(currentUserId, otherUserId)) {
            throw new InvalidConnectionActionException("Cannot disconnect from yourself");
        }

        long a = Math.min(currentUserId, otherUserId);
        long b = Math.max(currentUserId, otherUserId);

        if (!connections.existsByUserAIdAndUserBId(a, b)) {
            throw new ConnectionNotFoundException("Connection not found");
        }

        connections.deleteByUserAIdAndUserBId(a, b);

        requests.deleteByFromUserIdAndToUserId(currentUserId, otherUserId);
        requests.deleteByFromUserIdAndToUserId(otherUserId, currentUserId);

        var conversation = conversations.findByUserAIdAndUserBId(a, b);
        if (conversation.isPresent()) {
            conversations.delete(conversation.get());
            conversations.flush();
        }
    }

    public ConnectionStatusDto getConnectionStatus(Long userId) {
        List<Connection> myConns = connections.findByUserAIdOrUserBId(userId, userId);

        Set<Long> connectionUserIds = myConns.stream()
                .map(c -> Objects.equals(c.getUserAId(), userId) ? c.getUserBId() : c.getUserAId())
                .collect(Collectors.toSet());

        List<ConnectionRequest> incoming = requests.findByToUserIdAndStatus(userId, ConnectionRequestStatus.PENDING);
        List<ConnectionRequest> outgoing = requests.findByFromUserIdAndStatus(userId, ConnectionRequestStatus.PENDING);

        Set<Long> incomingUserIds = incoming.stream()
                .map(ConnectionRequest::getFromUserId)
                .collect(Collectors.toSet());

        Set<Long> outgoingUserIds = outgoing.stream()
                .map(ConnectionRequest::getToUserId)
                .collect(Collectors.toSet());

        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(connectionUserIds);
        allUserIds.addAll(incomingUserIds);
        allUserIds.addAll(outgoingUserIds);

        Map<Long, User> userMap = users.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, Profile> profileMap = profiles.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        List<UserDto> connectionDtos = connectionUserIds.stream()
                .map(id -> toUserDto(userMap.get(id), profileMap.get(id)))
                .filter(Objects::nonNull)
                .toList();

        List<ConnectionRequestDto> incomingDtos = incoming.stream()
                .map(req -> toConnectionRequestDto(req, userMap.get(req.getFromUserId()), profileMap.get(req.getFromUserId())))
                .filter(Objects::nonNull)
                .toList();

        List<ConnectionRequestDto> outgoingDtos = outgoing.stream()
                .map(req -> toConnectionRequestDto(req, userMap.get(req.getToUserId()), profileMap.get(req.getToUserId())))
                .filter(Objects::nonNull)
                .toList();

        List<Long> friendIds = new ArrayList<>(connectionUserIds);
        Map<Long, Long> unreadCounts = messagingService.getUnreadCounts(userId, friendIds);

        return new ConnectionStatusDto(connectionDtos, incomingDtos, outgoingDtos, unreadCounts);
    }

    private UserDto toUserDto(User u, Profile p) {
        return userDtoMapper.toDto(u, p);
    }

    private ConnectionRequestDto toConnectionRequestDto(ConnectionRequest req, User u, Profile p) {
        if (u == null) {
            return null;
        }
        UserDto userDto = userDtoMapper.toDto(u, p);
        return new ConnectionRequestDto(
                req.getId(),
                userDto.id(),
                userDto.displayName(),
                userDto.avatarUrl(),
                userDto.profileVisibility(),
                userDto.campusDomain()
        );
    }
}
