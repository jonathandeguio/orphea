package io.movetodata.passport.controllers;

import io.movetodata.passport.library.repository.LoginHistoryRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.platform.library.repository.PlatformConfigRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Session", description = "Session activity and timeout management")
public class SessionController {

    private final LoginHistoryRepository loginHistoryRepository;
    private final PlatformConfigRepository platformConfigRepository;
    private final UserService userService;
    private final AuthzService authzService;

    @PostMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();
        Date now = new Date();

        loginHistoryRepository
                .findFirstByUserIdAndLastLogoutAtIsNullOrderByLastLoginAtDesc(userId)
                .ifPresent(session -> {
                    session.setLastActivityAt(now);
                    loginHistoryRepository.save(session);
                });

        int timeoutMinutes = getSessionTimeoutMinutes();

        Map<String, Object> response = new HashMap<>();
        response.put("lastActivityAt", now.getTime());
        response.put("sessionTimeoutMinutes", timeoutMinutes);
        response.put("expiresAt", now.getTime() + (long) timeoutMinutes * 60 * 1000);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSessionConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("sessionTimeoutMinutes", getSessionTimeoutMinutes());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/config")
    public ResponseEntity<Object> updateSessionConfig(
            Principal principal,
            @RequestBody Map<String, Integer> body) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isPlatformAdmin(userId)) {
            return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);
        }

        Integer minutes = body.get("sessionTimeoutMinutes");
        if (minutes == null || minutes < 1 || minutes > 1440) {
            return new ResponseEntity<>("sessionTimeoutMinutes must be between 1 and 1440", HttpStatus.BAD_REQUEST);
        }

        platformConfigRepository.findByName("platformConfig").ifPresent(config -> {
            config.setSessionTimeoutMinutes(minutes);
            platformConfigRepository.save(config);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("sessionTimeoutMinutes", minutes);
        return ResponseEntity.ok(response);
    }

    private int getSessionTimeoutMinutes() {
        return platformConfigRepository.findByName("platformConfig")
                .map(c -> c.getSessionTimeoutMinutes() != null ? c.getSessionTimeoutMinutes() : 30)
                .orElse(30);
    }
}
