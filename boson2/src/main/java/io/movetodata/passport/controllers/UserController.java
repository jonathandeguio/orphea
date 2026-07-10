package io.movetodata.passport.controllers;

import io.movetodata.passport.exception.ResourceNotFoundException;
import io.movetodata.passport.library.models.UpdateMetadataRequest;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.repository.LoginHistoryRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.CurrentUser;
import io.movetodata.passport.security.UserPrincipal;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.*;

import static io.movetodata.sharedUtils.Utils.copyNonNullProperties;


@RestController
@RequestMapping("/api/passport/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final LoginHistoryRepository loginHistoryRepository;

    private final OkResponse response = new OkResponse();

    @Operation(summary = "It provides list of all users")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getUsersAll(@CurrentUser UserPrincipal userPrincipal) {

        return ResponseEntity.ok().body(userService.getUsers());
    }


    @Operation(summary = "To add new users")
    @PostMapping("/add")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/user/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }

    @Operation(summary = "It updates User")
    @PostMapping("/updateMetaData")
    public ResponseEntity<Object> updateMetaData(Principal principal, @RequestBody UpdateMetadataRequest updateMetadataRequest) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (userRepository.existsById(updateMetadataRequest.getId())) {
            User user = userRepository.getReferenceById(updateMetadataRequest.getId());
            user.setUpdatedBy(userId);
            user.setUpdatedAt(new Date());

            // Update fields only if they are different
            if (updateMetadataRequest.getGivenName() != null ) {
                if (!Objects.equals(user.getGivenName(), updateMetadataRequest.getGivenName())) {
                    user.setGivenName(updateMetadataRequest.getGivenName());
                }
            }

            if (updateMetadataRequest.getFamilyName() != null ) { if (!Objects.equals(user.getFamilyName(), updateMetadataRequest.getFamilyName())) {
                user.setFamilyName(updateMetadataRequest.getFamilyName());
            }
            }

            if (updateMetadataRequest.getGivenName() != null ) { if (!Objects.equals(user.getLanguage(), updateMetadataRequest.getLanguage())) {
                user.setLanguage(updateMetadataRequest.getLanguage());
            }
            }

            if (updateMetadataRequest.getMode() != null ) { if (!Objects.equals(user.getMode(), updateMetadataRequest.getMode())) {
                user.setMode(updateMetadataRequest.getMode());
            }
            }

            if (updateMetadataRequest.getEmail() != null ) { if (!Objects.equals(user.getEmail(), updateMetadataRequest.getEmail())) {
                user.setEmail(updateMetadataRequest.getEmail());
            }
            }

            if (updateMetadataRequest.getLocation() != null ) { if (!Objects.equals(user.getLocation(), updateMetadataRequest.getLocation())) {
                user.setLocation(updateMetadataRequest.getLocation());
            }
            }

            if (updateMetadataRequest.getPassword() != null ) { if (!Objects.equals(user.getPassword(), updateMetadataRequest.getPassword())) {
                user.setPassword(updateMetadataRequest.getPassword());
            }
            }

            userRepository.save(user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User with ID " + updateMetadataRequest.getId() + " does not exist", HttpStatus.NOT_FOUND);
        }
    }


    @Operation(summary = "It provides groups related to existing user")
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public ResponseEntity<User> getMe(Principal principal) {

        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + principal.getName())
        );


        return ResponseEntity.ok().body(user);

    }

    @Operation(summary = "It provides details about user by userID")
    @GetMapping("/{userId}")
    ResponseEntity<Object> userById(Principal principal, @PathVariable("userId") UUID userId) {
        return ResponseEntity.ok().body(userRepository.findById(userId));
    }

    @Operation(summary = "It provides details about user by userID")
    @DeleteMapping("/{userId}")
    ResponseEntity<Object> deleteUserById(Principal principal, @PathVariable("userId") List<UUID> userIds) {

        for (UUID userId : userIds) {
            if (!userRepository.existsById(userId)) {
                return new ResponseEntity<>("group with Id " + userId + " does not exist", HttpStatus.NOT_FOUND);
            }
            userRepository.deleteById(userId);
        }

        return ResponseEntity.ok().body("User deleted successfully");
    }

    @Operation(summary = "It provides login history of user")
    @GetMapping("/{userId}/loginHistory")
    ResponseEntity<Object> loginHistory(Principal principal, @PathVariable("userId") UUID userId) {
        return ResponseEntity.ok().body(loginHistoryRepository.findByUserId(userId));
    }

    @Operation(summary = "It provides login history of user last 10")
    @GetMapping("/{userId}/last10Login")
    ResponseEntity<Object> last10Login(Principal principal, @PathVariable("userId") UUID userId) {
        return ResponseEntity.ok().body(loginHistoryRepository.findTop10ByUserIdOrderByLastLoginAtDesc(userId));
    }

    @Operation(summary = "Gives true or false if the logged in user is project administrator or not")
    @GetMapping("/isProjectAdministrator")
    ResponseEntity<Object> isProjectAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (authzService.checkSystemPermissions(userId, new UUID(0, 0), "project-administrators")) {
            return ResponseEntity.ok().body(true);
        }

        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is group administrator or not")
    @GetMapping("/isGroupAdministrator")
    ResponseEntity<Object> isGroupAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (authzService.checkSystemPermissions(userId, new UUID(1, 1), "group-administrators")) {
            return ResponseEntity.ok().body(true);
        }

        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is user administrator or not")
    @GetMapping("/isUserAdministrator")
    ResponseEntity<Object> isUserAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (authzService.checkSystemPermissions(userId, new UUID(2, 2), "user-administrators")) {
            return ResponseEntity.ok().body(true);
        }

        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is user administrator or not")
    @GetMapping("/isIgniteAdministrator")
    ResponseEntity<Object> isIgniteAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is user administrator or not")
    @GetMapping("/userResourcePermissions/{Id}")
    ResponseEntity<Object> userResourcePermissions(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).id;

        HashMap<String, Object> permissions = new HashMap<>();

        permissions.put("owner", authzService.isOwner(userId, Id));
        permissions.put("editor", authzService.isEditor(userId, Id));
        permissions.put("viewer", authzService.isViewer(userId, Id));

        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @Operation(summary = "It provides details about user by userID")
    @PostMapping("/byIds")
    ResponseEntity<Object> userByIds(Principal principal, @RequestBody List<UUID> userIds) {


        Set<UUID> set = new HashSet<>(userIds);
        userIds.clear();
        userIds.addAll(set);

        Map<UUID, Object> responseIds = new HashMap<>();

        for (UUID Id : userIds) {

            if (!userRepository.existsById(Id)) {
                return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);

            }
            responseIds.put(Id, userRepository.findById(Id).get());
        }

        return new ResponseEntity<>(responseIds, HttpStatus.OK);


    }
}
