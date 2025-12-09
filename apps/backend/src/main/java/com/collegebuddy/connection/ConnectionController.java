package com.collegebuddy.connection;

import com.collegebuddy.dto.ConnectionStatusDto;
import com.collegebuddy.dto.RespondToConnectionDto;
import com.collegebuddy.dto.SendConnectionRequestDto;
import com.collegebuddy.security.AuthenticatedUser;
import com.collegebuddy.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/request")
    public ResponseEntity<Void> requestConnection(@RequestBody SendConnectionRequestDto dto) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        connectionService.sendConnectionRequest(current.id(), current.campusDomain(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/respond")
    public ResponseEntity<Void> respond(@RequestBody RespondToConnectionDto dto) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        connectionService.respondToConnectionRequest(current.id(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ConnectionStatusDto> listConnections() {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        ConnectionStatusDto status = connectionService.getConnectionStatus(current.id());
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> disconnect(@PathVariable Long userId) {
        AuthenticatedUser current = SecurityUtils.getCurrentUser();
        connectionService.disconnect(current.id(), userId);
        return ResponseEntity.ok().build();
    }
}
