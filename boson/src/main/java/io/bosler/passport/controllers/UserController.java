package io.bosler.passport.controllers;

import io.bosler.kitab.library.models.ProjectWithUserRoleDTO;
import io.bosler.passport.DTO.GroupDTO;
import io.bosler.passport.DTO.UserDTO;
import io.bosler.passport.DTO.UserProjectsDTO;
import io.bosler.passport.enums.AuthProvider;
import io.bosler.passport.enums.UserData;
import io.bosler.passport.exception.ResourceNotFoundException;
import io.bosler.passport.library.Auth;
import io.bosler.passport.library.models.*;
import io.bosler.passport.library.repository.*;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.GroupService;
import io.bosler.passport.library.service.UserHandlerService;
import io.bosler.passport.library.service.UserService;
import io.bosler.passport.security.AuthUser;
import io.bosler.passport.security.CurrentUser;
import io.bosler.sharedutils.Response.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static io.bosler.passport.enums.PlatformUsers.PlatformInternal;


@RestController
@RequestMapping("/api/passport/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class UserController {
    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final GroupsRepository groupsRepository;
    private final GroupService groupService;
    private final UserHandlerService userHandlerService;

    @Operation(summary = "It provides list of all users")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getUsersAll() throws IOException {
        List<UserDTO> users = new ArrayList<>();
        for (User user : userService.getUsers()) {
            if (!user.getUsername().equals(PlatformInternal.toString())) {
                users.add(userService.convertUserIntoDTO(user));
            }
        }
        return ResponseEntity.ok().body(users);
    }


    @Operation(summary = "To add new users")
    @PostMapping("/add")
    @PreAuthorize(Auth.PLATFORM_OR_USER_ADMIN)
    public ResponseEntity<Object> saveUser(@RequestBody User user) {
        user.setProvider(AuthProvider.local);
        User createdUser = userHandlerService.createUser(user);
        return ResponseEntity.ok().body(createdUser);
    }

    @Operation(summary = "Get notification Preferences")
    @GetMapping("/getNotificationPreferences")
    public ResponseEntity<Object> getNotificationPreferences(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();
        User user = userRepository.getReferenceById(userId);
        NotificationPreferences notificationPreferences = new NotificationPreferences();
        if (user.getNotificationPreferencesId() != null) {
            notificationPreferences = notificationPreferencesRepository.getReferenceById(user.getNotificationPreferencesId());
        }
        notificationPreferencesRepository.save(notificationPreferences);
        user.setNotificationPreferencesId(notificationPreferences.getId());
        userRepository.save(user);

        return new ResponseEntity(notificationPreferences, HttpStatus.OK);
    }

    @Operation(summary = "To update notification Preferences")
    @PostMapping("/updateNotificationPreferences")
    public ResponseEntity<Object> updateNotificationPreferences(Principal principal, @RequestBody UpdateNotificationPreferences updateNotificationPreferences) {
        UUID userId = userService.getUser(principal.getName()).getId();
        User user = userRepository.getReferenceById(userId);

        NotificationPreferences notificationPreferences = notificationPreferencesRepository.getReferenceById(user.getNotificationPreferencesId());

        notificationPreferences.setSubscription(updateNotificationPreferences.isSubscription());
        notificationPreferences.setMention(updateNotificationPreferences.isMention());
        notificationPreferences.setAccessManager(updateNotificationPreferences.isAccessManager());
        notificationPreferencesRepository.save(notificationPreferences);

        return new ResponseEntity(notificationPreferences, HttpStatus.OK);
    }

    @Operation(summary = "To change password")
    @PostMapping("/changePassword")
    public ResponseEntity<Object> changePassword(Principal principal, @RequestBody ChangePasswordRequest changePasswordRequest) {

        UUID userId = userService.getUser(principal.getName()).getId();
        User authenticateUser = userService.getUser(principal.getName());

        // check if userId exists or not
        //  if the user is himself then he is allowed to change the password
        //  the platform-admin or user-admin can change password for anyone

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), authenticateUser.getPassword()))
            return new ResponseEntity("User's current password is incorrect", HttpStatus.BAD_REQUEST);

        if (!userRepository.existsById(changePasswordRequest.getUserId()))
            return new ResponseEntity("User Not Found", HttpStatus.NOT_FOUND);

        if (!userId.equals(changePasswordRequest.getUserId()) && !authzService.isPlatformAdmin(userId)
                && !authzService.isUserAdmin(userId))
            return new ResponseEntity("Access Denied to update password of the user", HttpStatus.FORBIDDEN);


        User user = userRepository.getReferenceById(changePasswordRequest.getUserId());
        userService.changePassword(user, changePasswordRequest.getPassword());

        return new ResponseEntity("Password Changed", HttpStatus.OK);
    }

    @Operation(summary = "It updates User Meta Data")
    @PostMapping("/updateMetaData")
    public ResponseEntity<Object> updateMetaData(Principal principal, @RequestBody UpdateMetadataRequest updateMetadataRequest) {
        UUID userId = userService.getUser(principal.getName()).getId();


        if (!userId.equals(updateMetadataRequest.getId()) && !authzService.isPlatformAdmin(userId)
                && !authzService.isUserAdmin(userId))
            return new ResponseEntity("Access Denied to update metaData of user", HttpStatus.FORBIDDEN);

        if (userRepository.existsById(updateMetadataRequest.getId())) {
            User user = userRepository.getReferenceById(updateMetadataRequest.getId());

            user.setUpdatedBy(userId);
            user.setUpdatedAt(new Date());

            // Update fields only if they are different
            if (updateMetadataRequest.getGivenName() != null) {
                if (!Objects.equals(user.getGivenName(), updateMetadataRequest.getGivenName())) {
                    user.setGivenName(updateMetadataRequest.getGivenName());
                }
            }

            if (updateMetadataRequest.getFamilyName() != null) {
                if (!Objects.equals(user.getFamilyName(), updateMetadataRequest.getFamilyName())) {
                    user.setFamilyName(updateMetadataRequest.getFamilyName());
                }
            }

            if (updateMetadataRequest.getEmail() != null) {
                if (!Objects.equals(user.getEmail(), updateMetadataRequest.getEmail())) {
                    user.setEmail(updateMetadataRequest.getEmail());
                }
            }

            if (updateMetadataRequest.getLocation() != null) {
                if (!Objects.equals(user.getLocation(), updateMetadataRequest.getLocation())) {
                    user.setLocation(updateMetadataRequest.getLocation());
                }
            }


            if (updateMetadataRequest.getPassword() != null) {
                if (!Objects.equals(user.getPassword(), updateMetadataRequest.getPassword())) {
                    user.setPassword(updateMetadataRequest.getPassword());
                }
            }

            if (updateMetadataRequest.getIsMfaSkipped() != null) {
                user.setIsMfaSkipped(updateMetadataRequest.getIsMfaSkipped());
            }

            if (updateMetadataRequest.getMfaSkippedDate() != null) {
                user.setMfaSkippedAt(updateMetadataRequest.getMfaSkippedDate());
            }

            if (!Objects.equals(user.getProfileImage(), updateMetadataRequest.getProfileImage())) {
                try {
                    user.setProfileImage(userService.compressProfileImage(updateMetadataRequest.getProfileImage(), 0.5f));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            UserPreferences userPreferences = user.getPreferences();

            if (updateMetadataRequest.getCMDOpen() != null)
                userPreferences.setCMDOpen(updateMetadataRequest.getCMDOpen());

            if (updateMetadataRequest.getSearchOpen() != null)
                userPreferences.setSearchOpen(updateMetadataRequest.getSearchOpen());

            if (updateMetadataRequest.getMap() != null)
                userPreferences.setMap(updateMetadataRequest.getMap());

            if (updateMetadataRequest.getAutoFormatSQL() != null)
                userPreferences.setAutoFormatSQL(updateMetadataRequest.getAutoFormatSQL());

            if (updateMetadataRequest.getFolderListView() != null)
                userPreferences.setFolderListView(updateMetadataRequest.getFolderListView());

            if (updateMetadataRequest.getFontSize() != null && !Objects.equals(updateMetadataRequest.getFontSize(), 0)) {
                userPreferences.setFontSize(updateMetadataRequest.getFontSize());
            }

            if (updateMetadataRequest.getLanguage() != null && !Objects.equals(updateMetadataRequest.getLanguage(), "string")) {
                userPreferences.setLanguage(updateMetadataRequest.getLanguage());
            }

            if (updateMetadataRequest.getMode() != null && !Objects.equals(updateMetadataRequest.getMode(), "string")) {
                userPreferences.setMode(updateMetadataRequest.getMode());
            }

            if (updateMetadataRequest.getTimestampFormat() != null && !Objects.equals(updateMetadataRequest.getTimestampFormat(), "string")) {
                userPreferences.setTimestampFormat(updateMetadataRequest.getTimestampFormat());
            }

            if(updateMetadataRequest.getLayoutView() != null) {
                userPreferences.setLayoutView(updateMetadataRequest.getLayoutView());
            }

            if (updateMetadataRequest.getSidePanelOpen() != null) {
                userPreferences.setSidePanelOpen(updateMetadataRequest.getSidePanelOpen());
            }

            if (updateMetadataRequest.getCustomTheme() != null) {
                userPreferences.setCustomTheme(updateMetadataRequest.getCustomTheme());
            }

            userPreferencesRepository.save(userPreferences);
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User with ID " + updateMetadataRequest.getId() + " does not exist", HttpStatus.NOT_FOUND);
        }
    }


    @Operation(summary = "It provides groups related to existing user")
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public User getMe(@CurrentUser AuthUser authUser) {
        return userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authUser.getName()));
    }

    @Operation(summary = "It provides details about user by userID")
    @GetMapping("/{userId}")
    ResponseEntity<Object> userById(Principal principal, @PathVariable("userId") UUID Id) {
        //any user can atleast get details of other user
        return ResponseEntity.ok().body(userRepository.findById(Id));
    }

    @Operation(summary = "It Deletes the user by userId")
    @DeleteMapping("/{userId}")
    ResponseEntity<Object> deleteUserById(Principal principal, @PathVariable("userId") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isPlatformAdmin(userId)
                && !authzService.isUserAdmin(userId))
            return new ResponseEntity<>("Access Denied to Delete the user", HttpStatus.FORBIDDEN);


        if (!userRepository.existsById(Id)) {
            return new ResponseEntity<>("group with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (Objects.equals(userRepository.getReferenceById(Id).getUsername(), "platform-administrator"))
            return new ResponseEntity<>("Platform Administrator can not be deleted.", HttpStatus.FORBIDDEN);

        List<Groups> groups = groupsRepository.findAll();
        List<UUID> userIds = Arrays.asList(Id);
        for (Groups G : groups) {
            groupService.removeUsersToGroupMembers(userId, userIds, G.getId());
            groupService.removeUsersToGroupManagers(userId, userIds, G.getId());
            groupService.removeUsersToGroupOwners(userId, userIds, G.getId());
        }
        userRepository.deleteById(Id);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @Operation(summary = "It provides login history of user")
    @GetMapping("/{userId}/loginHistory")
    ResponseEntity<Object> loginHistory(Principal principal, @PathVariable("userId") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!userId.equals(Id) && !authzService.isPlatformAdmin(userId)
                && !authzService.isUserAdmin(userId))
            return new ResponseEntity<>("Access Denied to get login History", HttpStatus.FORBIDDEN);

        return ResponseEntity.ok().body(loginHistoryRepository.findByUserId(Id));
    }

    @Operation(summary = "It provides login history of user last 100")
    @GetMapping("/{userId}/last10Login")
    ResponseEntity<Object> last10Login(Principal principal, @PathVariable("userId") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!userId.equals(Id) && !authzService.isPlatformAdmin(userId)
                && !authzService.isUserAdmin(userId))
            return new ResponseEntity<>("Access Denied to get login History", HttpStatus.FORBIDDEN);

        return ResponseEntity.ok().body(loginHistoryRepository.findTop100ByUserIdOrderByLastLoginAtDesc(Id));
    }

    @Operation(summary = "Gives true or false if the logged in user is project administrator or not")
    @GetMapping("/isProjectAdministrator")
    ResponseEntity<Object> isProjectAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (authzService.isPlatformAdmin(userId))
            return ResponseEntity.ok().body(true);

        if (authzService.isProjectAdmin(userId)) {
            return ResponseEntity.ok().body(true);
        }

        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is group administrator or not")
    @GetMapping("/isGroupAdministrator")
    ResponseEntity<Object> isGroupAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (authzService.isPlatformAdmin(userId))
            return ResponseEntity.ok().body(true);

        if (authzService.isGroupAdmin(userId)) {
            return ResponseEntity.ok().body(true);
        }

        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is user administrator or not")
    @GetMapping("/isUserAdministrator")
    ResponseEntity<Object> isUserAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (authzService.isPlatformAdmin(userId))
            return ResponseEntity.ok().body(true);

        if (authzService.isUserAdmin(userId)) {
            return ResponseEntity.ok().body(true);
        }

        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is connect administrator or not")
    @GetMapping("/isConnectAdministrator")
    ResponseEntity<Object> isConnectAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (authzService.isPlatformAdmin(userId))
            return ResponseEntity.ok().body(true);

        if (authzService.isConnectAdmin(userId)) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is platform administrator or not")
    @GetMapping("/isPlatformAdministrator")
    ResponseEntity<Object> isPlatformAdministrator(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (authzService.isPlatformAdmin(userId))
            return ResponseEntity.ok().body(true);
        return ResponseEntity.ok().body(false);
    }

    @Operation(summary = "Gives true or false if the logged in user is user administrator or not")
    @GetMapping("/userResourcePermissions/{Id}")
    ResponseEntity<Object> userResourcePermissions(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();

        HashMap<String, Object> permissions = new HashMap<>();

        permissions.put("owner", authzService.isOwner(userId, Id));
        permissions.put("editor", authzService.isEditor(userId, Id));
        permissions.put("viewer", authzService.isViewer(userId, Id));

        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @Operation(summary = "Gives the list of groups which user belongs to.")
    @GetMapping("/userGroups/{id}")
    public ResponseEntity<Object> userGroups(@PathVariable("id") UUID userId) {
        List<GroupDTO> groups = groupService.getGroupsByUserMembership(userId);
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }

    @Operation(summary = "It provides details about user by userID")
    @PostMapping("/byIds")
    ResponseEntity<Object> userByIds(Principal principal, @RequestBody List<UUID> userIds) {

        UUID userId = userService.getUser(principal.getName()).getId();

//        if(!authzService.checkSystemPermissions(userId,new UUID(0,0),"platform-administrators")
//                &&!authzService.checkSystemPermissions(userId,new UUID(2,2),"user-administrators"))
//            return new ResponseEntity<>("Access Denied to get Details of user",HttpStatus.FORBIDDEN);


        Set<UUID> set = new HashSet<>(userIds);
        userIds.clear();
        userIds.addAll(set);

        Map<UUID, Object> responseIds = new HashMap<>();

        for (UUID Id : userIds) {
            if (userRepository.existsById(Id)) {
                responseIds.put(Id, userRepository.findById(Id).get());
            }
        }

        return new ResponseEntity<>(responseIds, HttpStatus.OK);


    }

    @Operation(summary = "It creates user in bulk by reading a csv file")
    @PostMapping("/bulkUserCreation")
    ResponseEntity<Object> bulkUserCreation(Principal principal, @RequestParam("file") MultipartFile file) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isPlatformAdmin(userId))
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorDTO(HttpStatus.UNAUTHORIZED.value(),
                            "Incapable permissions to bulk user create"));

        try {
            byte[] bytes = file.getBytes();
            String completeData = new String(bytes);
            String[] rows = completeData.split("\\r?\\n");
            List<String> filteredLines = Arrays.stream(rows)
                    .filter(line -> !line.trim().isEmpty() && !line.matches("^,+$"))
                    .collect(Collectors.toList());
            List<Object> result = new ArrayList<>();
            for (int i = 1; i < filteredLines.size(); i++) {
                String[] columns = filteredLines.get(i).split(",");
                HashMap<String, String> result_dict = new HashMap<>();
                result_dict.put(UserData.email.name(), columns[0]);
                result_dict.put(UserData.userName.name(), columns[1]);
                result_dict.put(UserData.firstName.name(), columns[2]);
                result_dict.put(UserData.familyName.name(), columns[3]);

                if (result_dict.get(UserData.email.name()).isEmpty()) {
                    result_dict.put("status", "fail");
                    result_dict.put("message", "Email provided is Empty");
                    result.add(result_dict);
                    continue;
                }

                if (result_dict.get(UserData.userName.name()).isEmpty()) {
                    result_dict.put("status", "fail");
                    result_dict.put("message", "User Name provided is Empty");
                    result.add(result_dict);
                    continue;
                }

                if (result_dict.get(UserData.firstName.name()).isEmpty()) {
                    result_dict.put("status", "fail");
                    result_dict.put("message", "First Name provided is Empty");
                    result.add(result_dict);
                    continue;
                }

                if (result_dict.get(UserData.familyName.name()).isEmpty()) {
                    result_dict.put("status", "fail");
                    result_dict.put("message", "Family Name provided is Empty");
                    result.add(result_dict);
                    continue;
                }

//                if (EmailValidator.isValidEmail(result_dict.get(UserData.email.name()))) {
//                    result_dict.put("status", "fail");
//                    result_dict.put("message", "Email is not valid");
//                    result.add(result_dict);
//                    continue;
//                }

                if (userRepository.existsByUsername(result_dict.get(UserData.userName.name()))) {
                    result_dict.put("status", "error");
                    result_dict.put("message", "User already exists with same userName");
                    result.add(result_dict);
                    continue;
                }

                if (userRepository.existsByEmail(result_dict.get(UserData.email.name()))) {
                    result_dict.put("status", "error");
                    result_dict.put("message", "User already exists with same email");
                    result.add(result_dict);
                    continue;
                }

                User user = new User();
                user.setEmail(result_dict.get(UserData.email.name()));
                user.setUsername(result_dict.get(UserData.userName.name()));
                user.setGivenName(result_dict.get(UserData.firstName.name()));
                user.setName(result_dict.get(UserData.firstName.name())
                        + " " + result_dict.get(UserData.familyName.name()));
                user.setFamilyName(result_dict.get(UserData.familyName.name()));
                user.setPassword("changeme");
                user.setProvider(AuthProvider.local);
                user.setPreferences(new UserPreferences());
                userService.saveUser(user);

                result_dict.put("status", "success");
                result_dict.put("message", "User created successfully.");
                result.add(result_dict);
            }
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDTO(HttpStatus.BAD_REQUEST.value(),
                        "File was empty"));
    }

    @Operation(summary = "It provides paginated list of user groups with their role.")
    @GetMapping("/userProjects/{id}")
    public ResponseEntity<Object> yourProjects(@PathVariable("id") UUID userId) {
        List<ProjectWithUserRoleDTO> projectsWithUserRole = new ArrayList<>();
        boolean isPlatformOrUserAdmin = false;
        if (authzService.isPlatformAdmin(userId) || authzService.isProjectAdmin(userId))
            isPlatformOrUserAdmin = true;
        else projectsWithUserRole.addAll(userService.getUserProjectWithUserRole(userId));
        return ResponseEntity.ok().body(UserProjectsDTO.builder()
                .isProjectOrPlatformAdmin(isPlatformOrUserAdmin)
                .projectList(projectsWithUserRole).build());
    }

}
