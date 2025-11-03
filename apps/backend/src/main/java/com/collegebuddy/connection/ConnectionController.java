package com.collegebuddy.connection;

import com.collegebuddy.dto.ConnectionStatusDto;
import com.collegebuddy.dto.RespondToConnectionDto;
import com.collegebuddy.dto.SendConnectionRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Sending/accepting/declining connection requests.
 */
@RestController
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/request")
    public ResponseEntity<ConnectionStatusDto> sendRequest(@RequestBody SendConnectionRequestDto request) {
        // TODO: create PENDING request
        return ResponseEntity.ok(new ConnectionStatusDto("PENDING"));
    }

    @PostMapping("/respond")
    public ResponseEntity<ConnectionStatusDto> respond(@RequestBody RespondToConnectionDto request) {
        // TODO: accept/decline
        return ResponseEntity.ok(new ConnectionStatusDto("ACCEPTED"));
    }
}
