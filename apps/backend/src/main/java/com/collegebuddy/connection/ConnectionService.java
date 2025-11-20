package com.collegebuddy.connection;

import com.collegebuddy.common.exceptions.ConnectionAlreadyExistsException;
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
import com.collegebuddy.repo.ConnectionRepository;
import com.collegebuddy.repo.ConnectionRequestRepository;
import com.collegebuddy.repo.ProfileRepository;
import com.collegebuddy.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectionService {

    private final ConnectionRepository connections;
    private final ConnectionRequestRepository requests;
    private final UserRepository users;
    private final ProfileRepository profiles;
    private final UserDtoMapper userDtoMapper;

    public ConnectionService(ConnectionRepository connections,
                             ConnectionRequestRepository requests,
                             UserRepository users,
                             ProfileRepository profiles,
                             UserDtoMapper userDtoMapper) {
        this.connections = connections;
        this.requests = requests;
        this.users = users;
        this.profiles = profiles;
        this.userDtoMapper = userDtoMapper;
    }

    public void sendConnectionRequest(Long requesterId, String requesterCampusDomain, SendConnectionRequestDto dto) {
        Long toUserId = dto.toUserId();

        if (Objects.equals(requesterId, toUserId)) {
            throw new InvalidConnectionActionException("Cannot connect to yourself");
        }

        User toUser = users.findById(toUserId)
                .orElseThrow(() -> new InvalidConnectionActionException("Target user not found"));

        if (!requesterCampusDomain.equalsIgnoreCase(toUser.getCampusDomain())) {
            throw new ForbiddenCampusAccessException("Cannot connect across campuses");
        }

        // normalize pair for connection existence check
        long a = Math.min(requesterId, toUserId);
        long b = Math.max(requesterId, toUserId);

        if (connections.existsByUserAIdAndUserBId(a, b)) {
            throw new ConnectionAlreadyExistsException("Users are already connected");
        }

        // prevent duplicate pending requests (in either direction)
        boolean pendingExists =
                requests.existsByFromUserIdAndToUserIdAndStatus(requesterId, toUserId, ConnectionRequestStatus.PENDING) ||
                        requests.existsByFromUserIdAndToUserIdAndStatus(toUserId, requesterId, ConnectionRequestStatus.PENDING);

        if (pendingExists) {
            throw new InvalidConnectionActionException("There is already a pending request between these users");
        }

        ConnectionRequest req = new ConnectionRequest();
        req.setFromUserId(requesterId);
        req.setToUserId(toUserId);
        req.setMessage(dto.message());
        req.setStatus(ConnectionRequestStatus.PENDING);
        req.setCreatedAt(Instant.now());

        requests.save(req);
    }

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

    public ConnectionStatusDto getConnectionStatus(Long userId) {
        // all connections where I am A or B
        List<Connection> myConns = connections.findByUserAIdOrUserBId(userId, userId);

        Set<Long> connectionUserIds = myConns.stream()
                .map(c -> Objects.equals(c.getUserAId(), userId) ? c.getUserBId() : c.getUserAId())
                .collect(Collectors.toSet());

        // pending incoming / outgoing
        List<ConnectionRequest> incoming = requests.findByToUserIdAndStatus(userId, ConnectionRequestStatus.PENDING);
        List<ConnectionRequest> outgoing = requests.findByFromUserIdAndStatus(userId, ConnectionRequestStatus.PENDING);

        Set<Long> incomingUserIds = incoming.stream()
                .map(ConnectionRequest::getFromUserId)
                .collect(Collectors.toSet());

        Set<Long> outgoingUserIds = outgoing.stream()
                .map(ConnectionRequest::getToUserId)
                .collect(Collectors.toSet());

        // load all users/profiles in one go
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

        return new ConnectionStatusDto(connectionDtos, incomingDtos, outgoingDtos);
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
                userDto.userId(),
                userDto.displayName(),
                userDto.avatarUrl(),
                userDto.visibility(),
                userDto.campusDomain()
        );
    }
}
