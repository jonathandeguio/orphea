package io.bosler.passport.controllers;

import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/passport/authz")
@Tag(name = "Passport", description = "Authz service endpoints")
public class AuthzController {
    private final UserService userService;
    private final AuthzService authzService;

    @Operation(summary = "Get resource permission")
    @GetMapping("/resourcePermission/{resourceId}")
    public ResponseEntity<Object> resourcePermission(Principal principal, @PathVariable("resourceId") UUID resourceId) {
        UUID userId = userService.getUser(principal.getName()).getId();
        String permission = "NONE";
        if (authzService.isOwner(userId, resourceId)) {
            permission = "OWNER";
        } else if (authzService.isEditor(userId, resourceId)) {
            permission = "EDITOR";
        } else if (authzService.isViewer(userId, resourceId)) {
            permission = "VIEWER";
        }

        return new ResponseEntity<>(permission, HttpStatus.OK);
    }
}
