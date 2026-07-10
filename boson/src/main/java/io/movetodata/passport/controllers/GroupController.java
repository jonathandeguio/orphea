package io.movetodata.passport.controllers;

import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.passport.DTO.GroupCategoriesDTO;
import io.movetodata.passport.DTO.GroupDTO;
import io.movetodata.passport.library.models.*;
import io.movetodata.passport.library.repository.GroupsRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/passport/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final AuthzService authzService;
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;

    @Operation(summary = "It provides list of all groups")
    @GetMapping("/all")
    public ResponseEntity<GroupCategoriesDTO> getAllGroups(@AuthenticationPrincipal AuthUser authUser) {
        UUID userId = authUser.getId();
        User user = userService.getUserById(userId);
        GroupCategoriesDTO groupCategoriesDTO = new GroupCategoriesDTO();
        List<GroupDTO> system= new ArrayList<>();
        List<GroupDTO> resource = new ArrayList<>();
        for (Groups G : groupService.getGroups()) {
            if (authzService.isPlatformAdmin(userId) || G.getMembers().contains(user) ||
                    G.getOwners().contains(user) || G.getManagers().contains(user)) {
                if (groupService.isGroupOneOfTheAdministrator(G.getName()))
                    system.add(groupService.convertGroupIntoDTO(G));
                else resource.add(groupService.convertGroupIntoDTO(G));
            }
        }
        groupCategoriesDTO.setSystem(system);
        groupCategoriesDTO.setResource(resource);
        return new ResponseEntity<>(groupCategoriesDTO, HttpStatus.OK);
    }

    @Operation(summary = "It provides list of all groups")
    @GetMapping("/getAllSystemGroups")
    public ResponseEntity<List<GroupDTO>> getAllSystemGroups() {
        List<GroupDTO> system = new ArrayList<>();
        for (Groups G : groupService.getGroups())
            if (groupService.isGroupOneOfTheAdministrator(G.getName()))
                system.add(groupService.convertGroupIntoDTO(G));
        return new ResponseEntity<>(system, HttpStatus.OK);
    }

    @Operation(summary = "It provides list of all groups")
    @GetMapping("/getAllResourceGroups")
    public ResponseEntity<List<GroupDTO>> getAllResourceGroups() {
        List<GroupDTO> resourceGroups = new ArrayList<>();
        for (Groups G : groupService.getGroups())
            if (!groupService.isGroupOneOfTheAdministrator(G.getName()))
                resourceGroups.add(groupService.convertGroupIntoDTO(G));
        return new ResponseEntity<>(resourceGroups, HttpStatus.OK);
    }

    @Operation(summary = "To add new groups")
    @PostMapping("/add")
    public ResponseEntity<Groups> saveGroup(Principal principal, @RequestBody Groups groups) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isPlatformAdmin(userId) && !authzService.isGroupAdmin(userId))
            return new ResponseEntity("Access Denied to create new group", HttpStatus.FORBIDDEN);

        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/group/add").toUriString());

        List<User> user1 = userRepository.findAllById(Collections.singleton(userId));

        groups.setCreatedAt(new Date());
        groups.setCreatedBy(userId);

        groups.setOwners(user1);

        Groups createdGroup = groupService.saveGroup(groups);

        return ResponseEntity.created(uri).body(createdGroup);
    }


    @Operation(summary = "It provides Groups by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getById(Principal principal, @PathVariable("Id") UUID Id) {


        if (!groupsRepository.existsById(Id)) {
            return new ResponseEntity<>("groups with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
        }

        Groups group = groupsRepository.findById(Id).get();
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();
        GroupWithUserDTO groupWithUserDTO = groupService.convertGroupIntoGroupWithUserDTO(group);

        if (!group.getMembers().contains(user) && !group.getManagers().contains(user) && !group.getOwners().contains(user) && !authzService.isPlatformAdmin(userId)
                && !authzService.isGroupAdmin(userId))
            return new ResponseEntity("Access Denied to get this group", HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(groupWithUserDTO, HttpStatus.OK);
    }

    @Operation(summary = "It deletes Groups by Id")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteById(Principal principal, @PathVariable("id") UUID Id) {
        User user = userService.getUser(principal.getName());
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!groupsRepository.existsById(Id))
            return new ResponseEntity<>("groups with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
        Groups group = groupsRepository.getReferenceById(Id);
        if (groupService.isGroupOneOfTheAdministrator(group.getName()))
            return new ResponseEntity<>("Administrator groups can not be deleted.", HttpStatus.FORBIDDEN);

        if (!authzService.isPlatformAdmin(userId)
                && !authzService.isGroupAdmin(userId) && !groupsRepository.findById(Id).get().getOwners().contains(user))
            return new ResponseEntity("Access Denied to delete the group", HttpStatus.FORBIDDEN);
        groupsRepository.deleteById(Id);


        return new ResponseEntity<>("Group deleted successfully.", HttpStatus.OK);
    }

    @Operation(summary = "Manage members or administrators to group")
    @PostMapping("/manageGroups")
    public ResponseEntity<Object> addOrRemoveUsersFromGroup(Principal principal, @RequestBody GroupManagementSpecification groupManagementSpecification) {
        User user = userService.getUser(principal.getName());
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!groupsRepository.existsById(groupManagementSpecification.getId())) {
            return new ResponseEntity<>("groups with Id " + groupManagementSpecification.getId() + " does not exist", HttpStatus.NOT_FOUND);
        }

        String action = groupManagementSpecification.getAction();
        if (!"add".equals(action) && !"remove".equals(action)) {
            return ResponseEntity.badRequest().body("Action has to be 'add' or 'remove'");
        }

        for (UUID groupUserId : groupManagementSpecification.getUserIds()) {
            if (!userRepository.existsById(groupUserId)) {
                return new ResponseEntity<>("User not found " + groupUserId, HttpStatus.NOT_FOUND);
            }
        }

        String type = groupManagementSpecification.getType();
        List<UUID> userIds = groupManagementSpecification.getUserIds();
        UUID groupId = groupManagementSpecification.getId();

        //checking if isOwner or isManager or platform admin or group admin
        if (!groupsRepository.getById(groupId).getOwners().contains(user)
                && !groupsRepository.getById(groupId).getManagers().contains(user)
                && !authzService.isPlatformAdmin(userId)
                && !authzService.isGroupAdmin(userId))
            return new ResponseEntity("Access Denied to Modify group", HttpStatus.FORBIDDEN);

        if ("add".equals(action)) {
            switch (type) {
                case "members":
                    groupService.addUsersToGroupMembers(userId, userIds, groupId);
                    break;
                case "managers":
                    groupService.addUsersToGroupManagers(userId, userIds, groupId);
                    break;
                case "owners":
                    groupService.addUsersToGroupOwners(userId, userIds, groupId);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid group type: " + type);
            }
        } else if ("remove".equals(action)) {
            switch (type) {
                case "members":
                    groupService.removeUsersToGroupMembers(userId, userIds, groupId);
                    break;
                case "managers":
                    groupService.removeUsersToGroupManagers(userId, userIds, groupId);
                    break;
                case "owners":
                    groupService.removeUsersToGroupOwners(userId, userIds, groupId);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid group type: " + type);
            }
        }
        Groups group = groupsRepository.findById(groupId).orElseThrow();
        return new ResponseEntity<>(group,HttpStatus.OK);
    }

    @Operation(summary = "This can be used to rename groups.")
    @PostMapping("/changeMetaData")
    public ResponseEntity<Object> renameGroup(@AuthenticationPrincipal AuthUser authUser, @RequestBody ChangeMetaData changeMetaData) {
        try {
            UUID userId = authUser.getId();
            User user = userRepository.getReferenceById(userId);
            UUID id = changeMetaData.getId();

            if (!groupsRepository.existsById(id)) {
                return new ResponseEntity<>("No group found for given Id", HttpStatus.NOT_FOUND);
            }

            Groups groups = groupsRepository.getReferenceById(id);

            if (changeMetaData.getName() != null &&
                    !changeMetaData.getName().isEmpty() &&
                    groupService.isGroupOneOfTheAdministrator(groups.getName()))
                return new ResponseEntity<>("Administrator groups can not be renamed.", HttpStatus.FORBIDDEN);


            if (!groups.getOwners().contains(user) && !authzService.isPlatformAdmin(userId)
                    && !authzService.isGroupAdmin(userId))
                return new ResponseEntity<>("Access Denied to change group metaData", HttpStatus.FORBIDDEN);

            if (changeMetaData.getName() != null && !changeMetaData.getName().isEmpty()) {
                if(groupService.isGroupOneOfTheAdministrator(changeMetaData.getName()))
                    return new ResponseEntity<>("Cannot change name to administrator group name", HttpStatus.FORBIDDEN);
                groups.setName(changeMetaData.getName());
            }

            if (changeMetaData.getDescription() != null)
                groups.setDescription(changeMetaData.getDescription());
            groupsRepository.save(groups);
            return new ResponseEntity<>("MetaData changed successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
