package io.movetodata.passport.controllers;

import io.movetodata.passport.library.models.Permissions;
import io.movetodata.passport.library.models.PermissionsMapping;
import io.movetodata.passport.library.models.Role;
import io.movetodata.passport.library.models.Users;
import io.movetodata.passport.library.repository.LoginHistoryRepo;
import io.movetodata.passport.library.repository.UserRepo;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//@CrossOrigin
//@EnableWebMvc
@RestController
@RequestMapping("/api/passport/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class UserController {
    private final UserRepo userRepo;
    private final UserService userService;
    private final LoginHistoryRepo loginHistoryRepo;
    private final OkResponse response = new OkResponse();

    @Operation(summary = "It provides list of all users")
    @GetMapping("/")
    public ResponseEntity<List<Users>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @Operation(summary = "It provides list of all users")
    @GetMapping("/all")
    public ResponseEntity<List<Users>> getUsersAll() {
        return ResponseEntity.ok().body(userService.getUsers());
    }


    @Operation(summary = "To add new users")
    @PostMapping("/add")
    public ResponseEntity<Users> saveUser(@RequestBody Users user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/user/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }

    @Operation(summary = "It provides groups related to existing user")
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    @ResponseBody
    public Users getMe(Principal principal) {
        return userService.getUser(principal.getName());
    }

    @Operation(summary = "It provides details about user by userID")
    @GetMapping("/{userId}")
    ResponseEntity<Object>userById(Principal principal, @PathVariable("userId") UUID userId) {
        return ResponseEntity.ok().body(userRepo.findById(userId));
    }

    @Operation(summary = "It provides login history of user")
    @GetMapping("/{userId}/loginHistory")
    ResponseEntity<Object>loginHistory(Principal principal, @PathVariable("userId") UUID userId) {
        return ResponseEntity.ok().body(loginHistoryRepo.findByUserId(userId));
    }

    @Operation(summary = "It provides login history of user last 10")
    @GetMapping("/{userId}/last10Login")
    ResponseEntity<Object>last10Login(Principal principal, @PathVariable("userId") UUID userId) {
        return ResponseEntity.ok().body(loginHistoryRepo.findTop10ByUserIdOrderByLastLoginAtDesc(userId));
    }

    @Operation(summary = "It provides details about user by userID")
    @PostMapping("/byIds")
    ResponseEntity<Object>userByIds(Principal principal, @RequestBody List<UUID> userIds) {


        Map<UUID , Object> responseIds = new HashMap<>();

        for(UUID Id : userIds) {

            if (!userRepo.existsById(Id)) {
                return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);

            }
            responseIds.put(Id, userRepo.findById(Id).get());
        }

        return new ResponseEntity<>(responseIds, HttpStatus.OK);


    }

    @Operation(summary = "It Save new Roles")
    @PostMapping("/role/save")
    public ResponseEntity<Role>saveRole(@RequestBody Role role){
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/users/role/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));
    }

    @Operation(summary = "It Save new permissions")
    @PostMapping("/Permissions/save")
    public ResponseEntity<Permissions>savePermissions(@RequestBody Permissions permissions){
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/users/permissions/save").toUriString());
        return ResponseEntity.created(uri).body(userService.savePermissions(permissions));
    }

    @Operation(summary = "It Save new permission mapping")
    @PostMapping("/PermissionsMapping/save")
    public ResponseEntity<PermissionsMapping>savePermissionsMapping(@RequestBody PermissionsMapping permissionsMapping){
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/users/permissionsMapping/save").toUriString());
        return ResponseEntity.created(uri).body(userService.savePermissionsMapping(permissionsMapping));
    }

    @Operation(summary = "It provides list of all Roles")
    @GetMapping("/Role/all")
    public ResponseEntity<List<Role>> getRoleAll() {
        return ResponseEntity.ok().body(userService.getRole());
    }

    @Operation(summary = "It provides list of all mapping")
    @GetMapping("/PermissionMapping/all")
    public ResponseEntity<List<PermissionsMapping>> getPermissionMappingAll() {
        return ResponseEntity.ok().body(userService.getPermissionsMapping());
    }


    @Operation(summary = "It provides list of all Permissions")
    @GetMapping("/Permission/all")
    public ResponseEntity<List<Permissions>> getPermissionAll() {
        return ResponseEntity.ok().body(userService.getPermissions());
    }

//    @PostMapping("/role/save")
//    public ResponseEntity<?>saveRole(@RequestBody RoleToPermissionForm form){
//        userService.saveRole(form.getRoleName(), form.getPermission());
//        return ResponseEntity.ok().build();
//    }

//    @Operation(summary = "It provides list of all groups")
//    @GetMapping("/groups")
//    public ResponseEntity<List<Groups>> getGroups() {
//        return ResponseEntity.ok().body(userService.getGroups());
//    }
//
//
//    @RequestMapping(value = "/me", method = RequestMethod.GET)
//    @ResponseBody
//    public Users getMe(Principal principal) {
//        return userService.getUser(principal.getName());
//    }

//    @GetMapping("/ssoAttributes")
//    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
//        return principal.getAttributes();
//        return Collections.singletonMap("ssoAttributes", principal.getAttributes());
//    }
}

@Data
class RoleToPermissionForm {
    private String RoleName;
    private String Permission;
}
