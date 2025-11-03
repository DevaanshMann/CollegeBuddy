package com.collegebuddy.connection;

import com.collegebuddy.dto.ConnectionStatusDto;
import com.collegebuddy.dto.RespondToConnectionDto;
import com.collegebuddy.dto.SendConnectionRequestDto;
import org.springframework.stereotype.Service;

/**
 * Handles lifecycle of connection requests and final connections.
 */
@Service
public class ConnectionService {

    public ConnectionStatusDto sendConnectionRequest(SendConnectionRequestDto request) {
        // TODO: create request if not duplicate
        return new ConnectionStatusDto("PENDING");
    }

    public ConnectionStatusDto respondToRequest(RespondToConnectionDto request) {
        // TODO: ACCEPT -> create Connection, DECLINE -> close request
        return new ConnectionStatusDto("ACCEPTED");
    }
}
